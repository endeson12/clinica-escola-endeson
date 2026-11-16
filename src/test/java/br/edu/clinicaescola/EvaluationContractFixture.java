package br.edu.clinicaescola;

import com.jayway.jsonpath.JsonPath;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.json.JsonMapper;
import java.util.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Synthetic fixtures only; execution by LEM (AI-assisted), not human authorship. */
@SpringBootTest(properties={"app.demo.admin.email=admin.teste@clinica.local","app.demo.admin.password=SenhaTeste#123","app.demo.recepcao.email=recepcao.teste@clinica.local","app.demo.recepcao.password=SenhaTeste#456"})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named="JDBC_DATABASE_URL",matches=".+")
abstract class EvaluationContractFixture {
 @Autowired MockMvc mvc;
 @Autowired JdbcTemplate jdbc;
 static final String B="/api/academico", PASSWORD="SenhaTeste#123";
 static final JsonMapper JSON=JsonMapper.builder().build();
 Cookie login(String email,String unit) throws Exception {
  long id=jdbc.queryForObject("select id from unidades where codigo=?",Long.class,unit);
  return mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json").content(JSON.writeValueAsString(Map.of("email",email,"senha",PASSWORD,"unidadeId",id))))
   .andExpect(status().isOk()).andReturn().getResponse().getCookie("CLINICA_SESSION");
 }
 ResultActions send(Cookie s,MockHttpServletRequestBuilder r,Map<String,Object> body) throws Exception {
  return mvc.perform(r.cookie(s).with(csrf()).contentType("application/json").content(JSON.writeValueAsString(body)));
 }
 long create(Cookie s,String route,Map<String,Object> body) throws Exception {
  String result=send(s,post(B+route),body).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
  return ((Number)JsonPath.read(result,"$.id")).longValue();
 }
 record Actor(long id,Cookie session) {}
 Actor actor(Cookie a,String role) throws Exception {
  String email=UUID.randomUUID()+"@queue.qa.local";
  long id=create(a,"/pessoas",Map.of("nome","QA "+role,"email",email,"senha",PASSWORD,"perfil",role));
  return new Actor(id,login(email,"JOQUEI"));
 }
 record Fixture(Cookie admin,Actor teacher,Actor student,long turma,long activity,long delivery) {}
 Fixture fixture() throws Exception {
  Cookie a=login("admin.teste@clinica.local","JOQUEI"); Actor p=actor(a,"PROFESSOR"),s=actor(a,"ALUNO");
  String tag=UUID.randomUUID().toString().substring(0,12);
  long c=create(a,"/cursos",Map.of("nome","QA","codigo",tag,"cargaHoraria",120));
  long d=create(a,"/disciplinas",Map.of("cursoId",c,"nome","QA","codigo",tag,"cargaHoraria",60));
  long t=create(a,"/turmas",Map.of("disciplinaId",d,"professorId",p.id(),"nome","QA "+tag,"periodo","2099.1","vagas",10));
  create(a,"/matriculas",Map.of("turmaId",t,"alunoId",s.id()));
  long at=activity(p.session(),t,"Contrato");
  long e=create(s.session(),"/entregas",Map.of("atividadeId",at,"conteudo","Resposta QA"));
  return new Fixture(a,p,s,t,at,e);
 }
 long activity(Cookie p,long turma,String title) throws Exception { return create(p,"/atividades",Map.of("turmaId",turma,"titulo",title,"instrucoes","Responder","prazo","2099-12-31T23:59:00Z")); }
 ResultActions grade(Fixture f,Object nota,String feedback,boolean published) throws Exception { return send(f.teacher().session(),put(B+"/entregas/"+f.delivery()+"/avaliacao"),Map.of("nota",nota,"feedback",feedback,"publicada",published)); }
 ResultActions queue(Cookie s,long activity,String state) throws Exception { return mvc.perform(get(B+"/entregas").param("atividadeId",Long.toString(activity)).param("situacao",state).cookie(s)); }
}
