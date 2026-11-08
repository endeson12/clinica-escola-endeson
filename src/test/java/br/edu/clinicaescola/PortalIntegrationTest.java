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

@SpringBootTest(properties = {"app.demo.admin.email=admin.teste@clinica.local", "app.demo.admin.password=SenhaTeste#123",
        "app.demo.recepcao.email=recepcao.teste@clinica.local", "app.demo.recepcao.password=SenhaTeste#456"})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named="JDBC_DATABASE_URL", matches=".+")
class PortalIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;
    private static final String BASE="/api/academico";
    private static final String SENHA="SenhaTeste#123";
    private static final JsonMapper JSON=JsonMapper.builder().build();
    private long unidade(String codigo) { return jdbc.queryForObject("select id from unidades where codigo=?", Long.class,codigo); }
    private String json(Map<String,Object> valor) { return JSON.writeValueAsString(valor); }
    private Cookie login(String email,String codigo) throws Exception {
        return mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                .content(json(Map.of("email",email,"senha",SENHA,"unidadeId",unidade(codigo)))))
                .andExpect(status().isOk()).andReturn().getResponse().getCookie("CLINICA_SESSION");
    }
    private Cookie admin() throws Exception { return login("admin.teste@clinica.local","JOQUEI"); }
    private ResultActions send(Cookie s,MockHttpServletRequestBuilder r,Map<String,Object> corpo) throws Exception {
        return mvc.perform(r.cookie(s).with(csrf()).contentType("application/json").content(json(corpo)));
    }
    private long create(Cookie s,String rota,Map<String,Object> corpo) throws Exception {
        String body=send(s,post(BASE+rota),corpo).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return ((Number)JsonPath.read(body,"$.id")).longValue();
    }
    private record Pessoa(long id,String email,Cookie sessao) {}
    private Pessoa pessoa(Cookie admin,String perfil) throws Exception {
        String email=UUID.randomUUID()+"@portal.teste.local";
        long id=create(admin,"/pessoas",Map.of("nome","Pessoa de teste "+perfil,"email",email,"senha",SENHA,"perfil",perfil));
        return new Pessoa(id,email,login(email,"JOQUEI"));
    }
    private record Turma(long curso,long disciplina,long turma,Pessoa professor,Pessoa aluno) {}
    private Turma turma(Cookie a) throws Exception {
        Pessoa p=pessoa(a,"PROFESSOR"), aluno=pessoa(a,"ALUNO");
        String uid=UUID.randomUUID().toString();
        long c=create(a,"/cursos",Map.of("nome","Curso teste","codigo",uid.substring(0,12),"cargaHoraria",120));
        long d=create(a,"/disciplinas",Map.of("cursoId",c,"nome","Disciplina teste","codigo",uid.substring(12,24),"cargaHoraria",60));
        long t=create(a,"/turmas",Map.of("disciplinaId",d,"professorId",p.id,"nome","Turma "+uid,"periodo","2026.2","vagas",30));
        create(a,"/matriculas",Map.of("turmaId",t,"alunoId",aluno.id));
        return new Turma(c,d,t,p,aluno);
    }
    @Test void contextoExigeLoginEPreservaPerfilAdmin() throws Exception {
        mvc.perform(get(BASE+"/contexto")).andExpect(status().isUnauthorized());
        mvc.perform(get(BASE+"/contexto").cookie(admin())).andExpect(status().isOk())
                .andExpect(jsonPath("$.usuario.perfil").value("ADMIN"));
    }
    @Test void jornadaCompletaDeEnsinoAvaliacaoFinanceiroECarteira() throws Exception {
        Cookie a=admin(); Turma t=turma(a);
        long aula=create(t.professor.sessao,"/aulas",Map.of("turmaId",t.turma,"titulo","Introdução","conteudo","Conteúdo da aula real persistido.","materialUrl","https://example.org/material"));
        long atividade=create(t.professor.sessao,"/atividades",Map.of("turmaId",t.turma,"titulo","Atividade avaliativa","instrucoes","Enviar resposta fundamentada.","prazo","2099-12-31T23:59:00Z"));
        mvc.perform(get(BASE+"/aulas?turmaId="+t.turma).cookie(t.aluno.sessao)).andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(aula));
        long e=create(t.aluno.sessao,"/entregas",Map.of("atividadeId",atividade,"conteudo","Resposta completa do estudante."));
        send(t.professor.sessao,put(BASE+"/entregas/"+e+"/avaliacao"),Map.of("nota",8.5,"feedback","Boa fundamentação.","publicada",false)).andExpect(status().isOk());
        mvc.perform(get(BASE+"/boletim").cookie(t.aluno.sessao)).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        send(t.professor.sessao,put(BASE+"/entregas/"+e+"/avaliacao"),Map.of("nota",8.5,"feedback","Boa fundamentação.","publicada",true)).andExpect(status().isOk());
        mvc.perform(get(BASE+"/boletim").cookie(t.aluno.sessao)).andExpect(status().isOk()).andExpect(jsonPath("$[0].nota").value(8.5)).andExpect(jsonPath("$[0].turma_id").value(t.turma));
        mvc.perform(get(BASE+"/carteirinha").cookie(t.aluno.sessao)).andExpect(status().isOk()).andExpect(jsonPath("$.oficial").value(false));
        long f=create(a,"/financeiro",Map.of("alunoId",t.aluno.id,"descricao","Mensalidade demonstrativa","valor",125.50,"vencimento","2099-12-01"));
        send(a,post(BASE+"/financeiro/"+f+"/baixa-demonstrativa"),Map.of()).andExpect(status().isOk()).andExpect(jsonPath("$.status").value("BAIXA_DEMONSTRATIVA"));
        mvc.perform(get(BASE+"/financeiro").cookie(t.aluno.sessao)).andExpect(status().isOk()).andExpect(jsonPath("$[0].demonstrativo").value(true));
        assertEquals(1,jdbc.queryForObject("select count(*) from academico_entregas where id=? and publicada",Integer.class,e));
    }
    @Test void alunoNaoAcessaColegasNemClinicaNemAdmin() throws Exception {
        Cookie a=admin(); Turma t=turma(a); Pessoa outro=pessoa(a,"ALUNO");
        for(String rota:List.of("/pessoas","/auditoria")) mvc.perform(get(BASE+rota).cookie(t.aluno.sessao)).andExpect(status().isForbidden());
        mvc.perform(get("/api/pacientes").cookie(t.aluno.sessao)).andExpect(status().isForbidden());
        mvc.perform(get(BASE+"/carteirinha?alunoId="+outro.id).cookie(t.aluno.sessao)).andExpect(status().isForbidden());
        mvc.perform(get(BASE+"/financeiro?alunoId="+outro.id).cookie(t.aluno.sessao)).andExpect(status().isForbidden());
        send(t.aluno.sessao,post(BASE+"/cursos"),Map.of("nome","Curso intruso","codigo","intruso","cargaHoraria",60)).andExpect(status().isForbidden());
    }
    @Test void professorNaoAcessaTurmaAlheiaOuFinanceiro() throws Exception {
        Cookie a=admin(); Turma t=turma(a); Pessoa outro=pessoa(a,"PROFESSOR");
        mvc.perform(get(BASE+"/aulas?turmaId="+t.turma).cookie(outro.sessao)).andExpect(status().isForbidden());
        mvc.perform(get(BASE+"/financeiro").cookie(t.professor.sessao)).andExpect(status().isForbidden());
        mvc.perform(get("/api/atendimentos").cookie(t.professor.sessao)).andExpect(status().isForbidden());
        send(outro.sessao,post(BASE+"/aulas"),Map.of("turmaId",t.turma,"titulo","Intrusão","conteudo","Sem vínculo.")).andExpect(status().isForbidden());
    }
    @Test void unidadeAlheiaBloqueiaIdsMesmoComAdmin() throws Exception {
        Cookie a=admin(); Turma t=turma(a); Cookie redencao=login("admin.teste@clinica.local","REDENCAO");
        mvc.perform(get(BASE+"/aulas?turmaId="+t.turma).cookie(redencao)).andExpect(status().isNotFound());
        send(redencao,post(BASE+"/disciplinas"),Map.of("cursoId",t.curso,"nome","Intrusão","codigo","outro","cargaHoraria",60)).andExpect(status().isNotFound());
    }
    @Test void matriculaDuplicadaProfessorInvalidoEPapelElevadoSaoNegados() throws Exception {
        Cookie a=admin(); Turma t=turma(a); Pessoa coordenador=pessoa(a,"COORDENACAO");
        send(a,post(BASE+"/matriculas"),Map.of("turmaId",t.turma,"alunoId",t.aluno.id)).andExpect(status().isConflict());
        send(a,post(BASE+"/turmas"),Map.of("disciplinaId",t.disciplina,"professorId",t.aluno.id,"nome","Inválida","periodo","2026.2","vagas",5)).andExpect(status().isBadRequest());
        send(coordenador.sessao,post(BASE+"/pessoas"),Map.of("nome","Elevação","email",UUID.randomUUID()+"@teste.local","senha",SENHA,"perfil","ADMIN")).andExpect(status().isForbidden());
        send(a,patch(BASE+"/pessoas/"+jdbc.queryForObject("select id from usuarios where email='admin.teste@clinica.local'",Long.class)),Map.of("nome","Admin","perfil","ALUNO","ativo",true)).andExpect(status().isConflict());
    }
    @Test void prazosNotasUrlsEEntregasDuplicadasSaoValidados() throws Exception {
        Cookie a=admin(); Turma t=turma(a);
        send(t.professor.sessao,post(BASE+"/aulas"),Map.of("turmaId",t.turma,"titulo","URL insegura","conteudo","Teste","materialUrl","javascript:alert(1)")).andExpect(status().isBadRequest());
        long atividade=create(t.professor.sessao,"/atividades",Map.of("turmaId",t.turma,"titulo","Entrega","instrucoes","Responder","prazo","2099-12-31T23:59:00Z"));
        long e=create(t.aluno.sessao,"/entregas",Map.of("atividadeId",atividade,"conteudo","Resposta"));
        send(t.aluno.sessao,post(BASE+"/entregas"),Map.of("atividadeId",atividade,"conteudo","Sobrescrever")).andExpect(status().isConflict());
        send(t.professor.sessao,put(BASE+"/entregas/"+e+"/avaliacao"),Map.of("nota",10.01,"feedback","Nota inválida","publicada",true)).andExpect(status().isBadRequest());
        long atrasada=create(t.professor.sessao,"/atividades",Map.of("turmaId",t.turma,"titulo","Prazo antigo","instrucoes","Responder","prazo","2000-01-01T00:00:00Z"));
        send(t.aluno.sessao,post(BASE+"/entregas"),Map.of("atividadeId",atrasada,"conteudo","Fora do prazo")).andExpect(status().isConflict());
    }
    @Test void financeiroNaoEntraNaDocenciaEAlunoSemMatriculaNaoGanhaCarteira() throws Exception {
        Cookie a=admin(); Pessoa financeiro=pessoa(a,"FINANCEIRO"),aluno=pessoa(a,"ALUNO");
        mvc.perform(get(BASE+"/pessoas").cookie(financeiro.sessao)).andExpect(status().isOk());
        mvc.perform(get(BASE+"/turmas").cookie(financeiro.sessao)).andExpect(status().isForbidden());
        mvc.perform(get(BASE+"/carteirinha").cookie(aluno.sessao)).andExpect(status().isNotFound());
        send(aluno.sessao,post(BASE+"/financeiro"),Map.of("alunoId",aluno.id,"descricao","Fraude","valor",1,"vencimento","2099-01-01")).andExpect(status().isForbidden());
        send(financeiro.sessao,post(BASE+"/financeiro"),Map.of("alunoId",aluno.id,"descricao","Negativo","valor",-1,"vencimento","2099-01-01")).andExpect(status().isBadRequest());
    }
    @Test void lotacaoDaTurmaESenhaFracaNaoPassam() throws Exception {
        Cookie a=admin(); Turma t=turma(a); Pessoa outro=pessoa(a,"ALUNO");
        send(a,put(BASE+"/turmas/"+t.turma),Map.of("disciplinaId",t.disciplina,"professorId",t.professor.id,"nome","Turma lotada "+UUID.randomUUID(),"periodo","2026.2","vagas",1)).andExpect(status().isOk());
        send(a,post(BASE+"/matriculas"),Map.of("turmaId",t.turma,"alunoId",outro.id)).andExpect(status().isConflict());
        send(a,post(BASE+"/pessoas"),Map.of("nome","Senha fraca","email",UUID.randomUUID()+"@teste.local","senha","123","perfil","ALUNO")).andExpect(status().isBadRequest());
    }
    @Test void professorAlheioNaoAvaliaEAlunoNaoLeRascunho() throws Exception {
        Cookie a=admin(); Turma t=turma(a); Pessoa outro=pessoa(a,"PROFESSOR");
        long at=create(t.professor.sessao,"/atividades",Map.of("turmaId",t.turma,"titulo","Teste de rascunho","instrucoes","Responder","prazo","2099-01-01T00:00:00Z"));
        long e=create(t.aluno.sessao,"/entregas",Map.of("atividadeId",at,"conteudo","Entrega"));
        send(outro.sessao,put(BASE+"/entregas/"+e+"/avaliacao"),Map.of("nota",7,"feedback","Intrusão","publicada",true)).andExpect(status().isForbidden());
        send(t.professor.sessao,put(BASE+"/entregas/"+e+"/avaliacao"),Map.of("nota",7,"feedback","Rascunho privado","publicada",false)).andExpect(status().isOk());
        mvc.perform(get(BASE+"/entregas?atividadeId="+at).cookie(t.aluno.sessao)).andExpect(status().isOk()).andExpect(jsonPath("$[0].nota").doesNotExist()).andExpect(jsonPath("$[0].feedback").doesNotExist());
        send(t.professor.sessao,put(BASE+"/atividades/"+at),Map.of("turmaId",t.turma,"titulo","Alteração tardia","instrucoes","Mudar regras","prazo","2099-01-01T00:00:00Z")).andExpect(status().isConflict());
    }
    @Test void recepcaoContinuaClinicaSemPoderCriarCurriculo() throws Exception {
        Pessoa r=pessoa(admin(),"RECEPCAO");
        for(String rota:List.of("/api/pacientes","/api/atendimentos","/api/especialidades","/api/dashboard"))
            mvc.perform(get(rota).cookie(r.sessao)).andExpect(status().isOk());
        mvc.perform(get(BASE+"/cursos").cookie(r.sessao)).andExpect(status().isForbidden());
        send(r.sessao,post(BASE+"/cursos"),Map.of("nome","Curso intruso","codigo","intruso","cargaHoraria",60)).andExpect(status().isForbidden());
    }
    @Test void revogacaoDePerfilValeNaMesmaSessaoEAuditoriaNaoVazaSenha() throws Exception {
        Cookie a=admin(); Pessoa aluno=pessoa(a,"ALUNO");
        jdbc.update("update academico_perfis set perfil='FINANCEIRO' where usuario_id=? and unidade_id=?",aluno.id,unidade("JOQUEI"));
        mvc.perform(get(BASE+"/turmas").cookie(aluno.sessao)).andExpect(status().isForbidden());
        mvc.perform(get(BASE+"/contexto").cookie(aluno.sessao)).andExpect(status().isOk()).andExpect(jsonPath("$.usuario.perfil").value("FINANCEIRO"));
        String audit=mvc.perform(get(BASE+"/auditoria").cookie(a)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertFalse(audit.contains(SENHA)); assertFalse(audit.contains("senha_hash"));
        mvc.perform(get(BASE+"/aulas?turmaId=nao-numerico").cookie(a)).andExpect(status().isBadRequest());
    }
    @Test void cancelamentoRevogaConteudoEMantemHistorico() throws Exception {
        Cookie a=admin(); Turma t=turma(a);
        Long m=jdbc.queryForObject("select id from academico_matriculas where turma_id=? and aluno_id=?",Long.class,t.turma,t.aluno.id);
        send(a,post(BASE+"/matriculas/"+m+"/cancelar"),Map.of()).andExpect(status().isOk());
        mvc.perform(get(BASE+"/aulas?turmaId="+t.turma).cookie(t.aluno.sessao)).andExpect(status().isForbidden());
        mvc.perform(get(BASE+"/carteirinha").cookie(t.aluno.sessao)).andExpect(status().isNotFound());
        assertEquals("CANCELADA",jdbc.queryForObject("select status from academico_matriculas where id=?",String.class,m));
    }
}
