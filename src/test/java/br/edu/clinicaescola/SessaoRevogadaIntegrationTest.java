package br.edu.clinicaescola;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "app.demo.admin.email=admin.teste@clinica.local",
        "app.demo.admin.password=SenhaTeste#123",
        "app.demo.recepcao.email=recepcao.teste@clinica.local",
        "app.demo.recepcao.password=SenhaTeste#456"})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "JDBC_DATABASE_URL", matches = ".+")
class SessaoRevogadaIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    private static final String[] ROTAS = {
            "/api/auth/me", "/api/pacientes", "/api/atendimentos",
            "/api/especialidades", "/api/dashboard"};

    private record UsuarioTeste(Long id, String email) {}

    private Long unidade(String codigo) {
        return jdbc.queryForObject("select id from unidades where codigo = ?", Long.class, codigo);
    }

    private UsuarioTeste usuario() {
        String email = "sessao-" + UUID.randomUUID() + "@teste.local";
        Long id = jdbc.queryForObject("""
                insert into usuarios (nome, email, senha_hash, ativo)
                select 'Usuario ficticio de regressao', ?, senha_hash, true
                from usuarios where email = 'admin.teste@clinica.local' returning id
                """, Long.class, email);
        jdbc.update("insert into usuarios_unidades (usuario_id, unidade_id) select ?, id from unidades", id);
        return new UsuarioTeste(id, email);
    }

    private Cookie login(UsuarioTeste u, String codigo) throws Exception {
        return mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                        .content("{\"unidadeId\":" + unidade(codigo) + ",\"email\":\"" + u.email()
                                + "\",\"senha\":\"SenhaTeste#123\"}"))
                .andExpect(status().isOk()).andReturn().getResponse().getCookie("CLINICA_SESSION");
    }

    @Test
    void usuarioDesativadoNaoPodeGravarMesmoComSessaoECsrfValidos() throws Exception {
        UsuarioTeste u = usuario();
        Cookie sessao = login(u, "JOQUEI");
        String nome = "Revogacao " + UUID.randomUUID();
        jdbc.update("update usuarios set ativo = false where id = ?", u.id());
        mvc.perform(post("/api/especialidades").cookie(sessao).with(csrf())
                        .contentType("application/json").content("{\"nome\":\"" + nome + "\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Sessão expirada. Faça login novamente."));
        assertEquals(0, jdbc.queryForObject("select count(*) from especialidades where nome = ?", Integer.class, nome));
        mvc.perform(get("/api/auth/me").cookie(sessao)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/health")).andExpect(status().isOk());
        mvc.perform(get("/api/auth/csrf")).andExpect(status().isOk());
    }

    @Test
    void remocaoDeVinculoBloqueiaUnidadeRevogadaSemBloquearOutraUnidade() throws Exception {
        UsuarioTeste u = usuario();
        Cookie joquei = login(u, "JOQUEI");
        Cookie redencao = login(u, "REDENCAO");
        jdbc.update("delete from usuarios_unidades where usuario_id = ? and unidade_id = ?", u.id(), unidade("JOQUEI"));
        mvc.perform(get("/api/pacientes").cookie(joquei)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").cookie(joquei)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").cookie(redencao)).andExpect(status().isOk())
                .andExpect(jsonPath("$.unidade.codigo").value("REDENCAO"));
        mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                        .content("{\"unidadeId\":" + unidade("JOQUEI") + ",\"email\":\"" + u.email()
                                + "\",\"senha\":\"SenhaTeste#123\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioExcluidoPerdeAcessoEmTodasAsRotasProtegidas() throws Exception {
        UsuarioTeste u = usuario();
        Cookie[] sessoes = new Cookie[ROTAS.length];
        for (int i = 0; i < ROTAS.length; i++) sessoes[i] = login(u, "JOQUEI");
        jdbc.update("delete from usuarios where id = ?", u.id());
        for (int i = 0; i < ROTAS.length; i++) {
            mvc.perform(get(ROTAS[i]).cookie(sessoes[i])).andExpect(status().isUnauthorized());
        }
    }

    @Test
    void usuarioAtivoComVinculoMantemLeituraEGravacao() throws Exception {
        UsuarioTeste u = usuario();
        Cookie sessao = login(u, "JOQUEI");
        for (String rota : ROTAS) mvc.perform(get(rota).cookie(sessao)).andExpect(status().isOk());
        String nome = "Controle valido " + UUID.randomUUID();
        mvc.perform(post("/api/especialidades").cookie(sessao).with(csrf()).contentType("application/json")
                        .content("{\"nome\":\"" + nome + "\"}"))
                .andExpect(status().isCreated());
        assertEquals(1, jdbc.queryForObject("select count(*) from especialidades where nome = ?", Integer.class, nome));
    }
}
