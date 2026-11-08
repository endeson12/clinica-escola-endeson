package br.edu.clinicaescola;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Isolated database fixtures, never institutional/student production data. */
@SpringBootTest(properties={"app.demo.admin.email=admin.teste@clinica.local","app.demo.admin.password=SenhaTeste#123",
 "app.demo.recepcao.email=recepcao.teste@clinica.local","app.demo.recepcao.password=SenhaTeste#456"})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named="JDBC_DATABASE_URL",matches=".+")
class PortalServicesIntegrationTest {
 @Autowired MockMvc mvc; @Autowired JdbcTemplate jdbc;
 static final String B="/api/academico", P="SenhaTeste#123";
 static final JsonMapper JSON=JsonMapper.builder().build();
 long unit(String code){return jdbc.queryForObject("select id from unidades where codigo=?",Long.class,code);}
 Cookie login(String email,String code)throws Exception{return mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
  .content(JSON.writeValueAsString(Map.of("email",email,"senha",P,"unidadeId",unit(code))))).andExpect(status().isOk()).andReturn().getResponse().getCookie("CLINICA_SESSION");}
 Cookie admin()throws Exception{return login("admin.teste@clinica.local","JOQUEI");}
 ResultActions send(Cookie c,MockHttpServletRequestBuilder r,Map<String,Object> body)throws Exception{return mvc.perform(r.cookie(c).with(csrf()).contentType("application/json").content(JSON.writeValueAsString(body)));}
 long create(Cookie c,String path,Map<String,Object> body)throws Exception{return ((Number)JsonPath.read(send(c,post(B+path),body).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString(),"$.id")).longValue();}
 record Person(long id,Cookie cookie){}
 Person person(Cookie a,String role)throws Exception{String email=UUID.randomUUID()+"@services.test.local";long id=create(a,"/pessoas",Map.of("nome","Fixture "+role,"email",email,"senha",P,"perfil",role));return new Person(id,login(email,"JOQUEI"));}
 @Test void profileIsPrivatePersistsAndDoesNotElevateRole()throws Exception{
  Cookie a=admin();Person s=person(a,"ALUNO"),other=person(a,"ALUNO");
  send(s.cookie,put(B+"/perfil"),Map.of("telefone","86900000000","endereco","Endereço de teste","cidade","Teresina","estado","PI","escola","Escola de teste","anoConclusao",2023,"perfil","ADMIN")).andExpect(status().isOk());
  mvc.perform(get(B+"/perfil").cookie(s.cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.telefone").value("86900000000")).andExpect(jsonPath("$.perfil").value("ALUNO")).andExpect(jsonPath("$.senha_hash").doesNotExist());
  mvc.perform(get(B+"/perfil").cookie(other.cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.telefone").value(""));
  send(s.cookie,put(B+"/perfil"),Map.of("telefone","x".repeat(50))).andExpect(status().isBadRequest());
  mvc.perform(put(B+"/perfil").cookie(s.cookie).contentType("application/json").content("{}")).andExpect(status().isForbidden());
 }
 @Test void requestLifecycleIsScopedAndNoOneCanAnswerOwnAsStudent()throws Exception{
  Cookie a=admin();Person s=person(a,"ALUNO"),other=person(a,"ALUNO");
  long id=create(s.cookie,"/solicitacoes",Map.of("tipo","DOCUMENTOS","assunto","Declaração de teste","mensagem","Preciso do documento para validar o fluxo."));
  mvc.perform(get(B+"/solicitacoes").cookie(s.cookie)).andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("ABERTA"));
  mvc.perform(get(B+"/solicitacoes").cookie(other.cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
  send(other.cookie,post(B+"/solicitacoes/"+id+"/cancelar"),Map.of()).andExpect(status().isNotFound());
  send(s.cookie,put(B+"/solicitacoes/"+id+"/resposta"),Map.of("resposta","Intrusão")).andExpect(status().isForbidden());
  send(login("admin.teste@clinica.local","REDENCAO"),put(B+"/solicitacoes/"+id+"/resposta"),Map.of("resposta","Outra unidade")).andExpect(status().isNotFound());
  send(a,put(B+"/solicitacoes/"+id+"/resposta"),Map.of("resposta","Resposta interna de homologação.")).andExpect(status().isOk());
  mvc.perform(get(B+"/solicitacoes").cookie(s.cookie)).andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("RESPONDIDA")).andExpect(jsonPath("$[0].resposta").value("Resposta interna de homologação."));
  send(s.cookie,post(B+"/solicitacoes/"+id+"/cancelar"),Map.of()).andExpect(status().isConflict());
 }
 @Test void ownerCanCancelOpenRequestAndUnknownCategoryIsRejected()throws Exception{
  Cookie a=admin();Person s=person(a,"ALUNO");
  long id=create(s.cookie,"/solicitacoes",Map.of("tipo","OUTROS","assunto","Cancelamento","mensagem","Teste de cancelamento."));
  send(s.cookie,post(B+"/solicitacoes/"+id+"/cancelar"),Map.of()).andExpect(status().isOk());
  send(s.cookie,post(B+"/solicitacoes"),Map.of("tipo","ROOT","assunto","Inválido","mensagem","Teste")).andExpect(status().isBadRequest());
 }
 @Test void complementaryHoursRequireReviewCannotLeakOrSelfApprove()throws Exception{
  Cookie a=admin();Person s=person(a,"ALUNO"),other=person(a,"ALUNO");
  long id=create(s.cookie,"/complementares",Map.of("titulo","Certificado de teste","data","2026-01-01","horas",4,"comprovanteUrl","https://example.org/certificado"));
  mvc.perform(get(B+"/complementares").cookie(s.cookie)).andExpect(status().isOk()).andExpect(jsonPath("$[0].status").value("PENDENTE")).andExpect(jsonPath("$[0].horas_aprovadas").value(0));
  mvc.perform(get(B+"/complementares").cookie(other.cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
  send(s.cookie,put(B+"/complementares/"+id+"/avaliacao"),Map.of("status","APROVADA","horasAprovadas",4,"parecer","Teste")).andExpect(status().isForbidden());
  send(a,put(B+"/complementares/"+id+"/avaliacao"),Map.of("status","APROVADA","horasAprovadas",5,"parecer","Horas excedentes")).andExpect(status().isBadRequest());
  send(a,put(B+"/complementares/"+id+"/avaliacao"),Map.of("status","APROVADA","horasAprovadas",3,"parecer","Conferência de teste")).andExpect(status().isOk());
  mvc.perform(get(B+"/complementares").cookie(s.cookie)).andExpect(status().isOk()).andExpect(jsonPath("$[0].horas_aprovadas").value(3));
  send(a,put(B+"/complementares/"+id+"/avaliacao"),Map.of("status","REPROVADA","horasAprovadas",0,"parecer","Não pode reavaliar silenciosamente")).andExpect(status().isConflict());
  send(s.cookie,post(B+"/complementares"),Map.of("titulo","URL perigosa","data","2026-01-01","horas",1,"comprovanteUrl","javascript:alert(1)")).andExpect(status().isBadRequest());
 }
 @Test void timetableHonorsEnrollmentUnitAndTimeValidation()throws Exception{
  Cookie a=admin();Person p=person(a,"PROFESSOR"),s=person(a,"ALUNO"),other=person(a,"ALUNO");String code=UUID.randomUUID().toString();
  long course=create(a,"/cursos",Map.of("nome","Curso horário","codigo",code.substring(0,12),"cargaHoraria",60));
  long d=create(a,"/disciplinas",Map.of("nome","Disciplina horário","codigo",code.substring(12,24),"cursoId",course,"cargaHoraria",30));
  long t=create(a,"/turmas",Map.of("nome","Turma "+code,"disciplinaId",d,"professorId",p.id,"periodo","2026.2","vagas",10));
  create(a,"/matriculas",Map.of("turmaId",t,"alunoId",s.id));
  long id=create(a,"/horarios",Map.of("turmaId",t,"dia",2,"inicio","18:30","fim","19:20","sala","Sala teste"));
  mvc.perform(get(B+"/horarios").cookie(s.cookie)).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
  mvc.perform(get(B+"/horarios").cookie(other.cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
  send(s.cookie,post(B+"/horarios"),Map.of("turmaId",t,"dia",2,"inicio","18:30","fim","19:20","sala","Intrusão")).andExpect(status().isForbidden());
  send(a,post(B+"/horarios"),Map.of("turmaId",t,"dia",2,"inicio","20:30","fim","19:20","sala","Inválido")).andExpect(status().isBadRequest());
  send(login("admin.teste@clinica.local","REDENCAO"),post(B+"/horarios/"+id+"/cancelar"),Map.of()).andExpect(status().isNotFound());
  send(a,post(B+"/horarios/"+id+"/cancelar"),Map.of()).andExpect(status().isOk());
  mvc.perform(get(B+"/horarios").cookie(s.cookie)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
 }
 @Test void newServicesRequireAuthenticationAndRespectReceptionPermissions()throws Exception{
  for(String path:List.of("/perfil","/solicitacoes","/complementares","/horarios"))mvc.perform(get(B+path)).andExpect(status().isUnauthorized());
  Person r=person(admin(),"RECEPCAO");for(String path:List.of("/complementares","/horarios"))mvc.perform(get(B+path).cookie(r.cookie)).andExpect(status().isForbidden());
  mvc.perform(get(B+"/perfil").cookie(r.cookie)).andExpect(status().isOk());
 }
}
