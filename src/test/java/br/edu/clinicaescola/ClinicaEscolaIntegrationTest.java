package br.edu.clinicaescola;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import jakarta.servlet.http.Cookie;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste de integracao contra um PostgreSQL REAL (migracoes Flyway + regras de negocio).
 * Roda somente quando JDBC_DATABASE_URL estiver definido (veja README).
 */
@SpringBootTest(properties = {
        "app.test.suite=clinica-csrf",
        "app.demo.admin.email=admin.teste@clinica.local",
        "app.demo.admin.password=SenhaTeste#123",
        "app.demo.recepcao.email=recepcao.teste@clinica.local",
        "app.demo.recepcao.password=SenhaTeste#456"})
@AutoConfigureMockMvc
@EnabledIfEnvironmentVariable(named = "JDBC_DATABASE_URL", matches = ".+")
class ClinicaEscolaIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired JdbcTemplate jdbc;

    private Long unidade(String codigo) {
        return jdbc.queryForObject("select id from unidades where codigo = ?", Long.class, codigo);
    }

    private Cookie login(String email, String senha, Long unidadeId) throws Exception {
        MvcResult r = mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                        .content("{\"unidadeId\":" + unidadeId + ",\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andExpect(status().isOk())
                .andExpect(cookie().httpOnly("CLINICA_SESSION", true))
                .andReturn();
        return r.getResponse().getCookie("CLINICA_SESSION");
    }

    @Test
    void healthPublicoConfirmaPostgresSemCredenciais() throws Exception {
        mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.aplicacao").value("UP"))
                .andExpect(jsonPath("$.banco").value("UP"))
                .andExpect(jsonPath("$.unidadesCadastradas").value(greaterThan(0)))
                .andExpect(jsonPath("$.migracaoAtual").isNotEmpty())
                .andExpect(jsonPath("$.*", hasSize(4)));
    }

    @Test
    void paginaDeLoginPublicaEntregaFormularioSemAutenticacao() throws Exception {
        mvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("type=\"password\"")))
                .andExpect(content().string(containsString("/js/auth.js")));
    }

    @Test
    void disponibilidadePublicaNaoLiberaDadosClinicosSemSessao() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk());
        mvc.perform(get("/api/pacientes")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/atendimentos")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginRenovaCsrfESessaoRejeitaCabecalhoAntigo() throws Exception {
        Cookie antes = mvc.perform(get("/api/auth/csrf"))
                .andExpect(status().isOk()).andReturn().getResponse().getCookie("XSRF-TOKEN");
        String credenciais = "{\"unidadeId\":" + unidade("JOQUEI")
                + ",\"email\":\"admin.teste@clinica.local\",\"senha\":\"SenhaTeste#123\"}";
        mvc.perform(post("/api/auth/login").contentType("application/json").content(credenciais))
                .andExpect(status().isForbidden());
        MvcResult autenticado = mvc.perform(post("/api/auth/login").cookie(antes)
                        .header("X-XSRF-TOKEN", antes.getValue())
                        .contentType("application/json").content(credenciais))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("XSRF-TOKEN", 0)).andReturn();
        Cookie sessao = autenticado.getResponse().getCookie("CLINICA_SESSION");
        Cookie depois = mvc.perform(get("/api/auth/csrf").cookie(sessao))
                .andExpect(status().isOk()).andReturn().getResponse().getCookie("XSRF-TOKEN");
        org.junit.jupiter.api.Assertions.assertNotEquals(antes.getValue(), depois.getValue());
        mvc.perform(post("/api/auth/logout").cookie(sessao, depois)
                        .header("X-XSRF-TOKEN", antes.getValue()))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/auth/me").cookie(sessao)).andExpect(status().isOk());
        mvc.perform(post("/api/auth/logout").cookie(sessao, depois)
                        .header("X-XSRF-TOKEN", depois.getValue()))
                .andExpect(status().isNoContent());
        mvc.perform(get("/api/auth/me").cookie(sessao)).andExpect(status().isUnauthorized());
    }

    @Test
    void fluxoCompletoComIsolamentoPorUnidade() throws Exception {
        Long joquei = unidade("JOQUEI");
        Long redencao = unidade("REDENCAO");
        String sufixo = String.valueOf(System.nanoTime() % 100000);

        // sem sessao: API bloqueada
        mvc.perform(get("/api/pacientes")).andExpect(status().isUnauthorized());

        // senha incorreta
        mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                        .content("{\"unidadeId\":" + joquei + ",\"email\":\"admin.teste@clinica.local\",\"senha\":\"errada\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("E-mail ou senha incorretos."));

        // e-mail invalido
        mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                        .content("{\"unidadeId\":" + joquei + ",\"email\":\"nao-e-email\",\"senha\":\"x\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());

        // unidade nao autorizada (recepcao so tem Joquei)
        mvc.perform(post("/api/auth/login").with(csrf()).contentType("application/json")
                        .content("{\"unidadeId\":" + redencao + ",\"email\":\"recepcao.teste@clinica.local\",\"senha\":\"SenhaTeste#456\"}"))
                .andExpect(status().isForbidden());

        Cookie sJ = login("admin.teste@clinica.local", "SenhaTeste#123", joquei);
        mvc.perform(get("/api/auth/me").cookie(sJ)).andExpect(status().isOk())
                .andExpect(jsonPath("$.unidade.codigo").value("JOQUEI"));

        // especialidade
        String esp = "Especialidade Teste " + sufixo;
        MvcResult e = mvc.perform(post("/api/especialidades").cookie(sJ).with(csrf()).contentType("application/json")
                        .content("{\"nome\":\"" + esp + "\",\"ativa\":true}"))
                .andExpect(status().isCreated()).andReturn();
        Long espId = Long.valueOf(e.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1"));
        mvc.perform(post("/api/especialidades").cookie(sJ).with(csrf()).contentType("application/json")
                        .content("{\"nome\":\"" + esp.toUpperCase() + "\"}"))
                .andExpect(status().isConflict());

        // CPF invalido e paciente valido
        mvc.perform(post("/api/pacientes").cookie(sJ).with(csrf()).contentType("application/json")
                        .content("{\"nomeCompleto\":\"Paciente X\",\"cpf\":\"111.111.111-11\",\"email\":\"x@teste.local\"}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.fieldErrors.cpf").value("CPF inválido."));
        String cpf = gerarCpf();
        MvcResult p = mvc.perform(post("/api/pacientes").cookie(sJ).with(csrf()).contentType("application/json")
                        .content("{\"nomeCompleto\":\"Paciente Teste " + sufixo + "\",\"cpf\":\"" + cpf + "\",\"email\":\"p" + sufixo + "@teste.local\"}"))
                .andExpect(status().isCreated()).andReturn();
        Long pacId = Long.valueOf(p.getResponse().getContentAsString().replaceAll(".*\"id\":(\\d+).*", "$1"));
        // CPF duplicado na mesma unidade
        mvc.perform(post("/api/pacientes").cookie(sJ).with(csrf()).contentType("application/json")
                        .content("{\"nomeCompleto\":\"Outro\",\"cpf\":\"" + cpf + "\",\"email\":\"o@teste.local\"}"))
                .andExpect(status().isConflict());

        // atendimento valido e com paciente inexistente
        mvc.perform(post("/api/atendimentos").cookie(sJ).with(csrf()).contentType("application/json")
                        .content("{\"pacienteId\":" + pacId + ",\"especialidadeId\":" + espId
                                + ",\"dataHora\":\"2026-11-10T09:30\",\"status\":\"AGENDADO\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paciente").value("Paciente Teste " + sufixo))
                .andExpect(jsonPath("$.especialidade").value(esp));
        mvc.perform(post("/api/atendimentos").cookie(sJ).with(csrf()).contentType("application/json")
                        .content("{\"pacienteId\":999999999,\"especialidadeId\":" + espId
                                + ",\"dataHora\":\"2026-11-10T09:30\",\"status\":\"AGENDADO\"}"))
                .andExpect(status().isUnprocessableContent());

        // outra unidade nao enxerga o paciente nem pode usa-lo
        Cookie sR = login("admin.teste@clinica.local", "SenhaTeste#123", redencao);
        mvc.perform(get("/api/pacientes").cookie(sR)).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", not(hasItem(pacId.intValue()))));
        mvc.perform(post("/api/atendimentos").cookie(sR).with(csrf()).contentType("application/json")
                        .content("{\"pacienteId\":" + pacId + ",\"especialidadeId\":" + espId
                                + ",\"dataHora\":\"2026-11-10T09:30\",\"status\":\"AGENDADO\"}"))
                .andExpect(status().isUnprocessableContent());

        // logout encerra a sessao
        mvc.perform(post("/api/auth/logout").cookie(sJ).with(csrf())).andExpect(status().isNoContent());
        mvc.perform(get("/api/pacientes").cookie(sJ)).andExpect(status().isUnauthorized());
    }

    private static String gerarCpf() {
        java.util.Random r = new java.util.Random();
        int[] d = new int[11];
        do {
            for (int i = 0; i < 9; i++) d[i] = r.nextInt(10);
        } while (java.util.Arrays.stream(d, 0, 9).distinct().count() == 1);
        for (int k = 9; k < 11; k++) {
            int s = 0;
            for (int i = 0; i < k; i++) s += d[i] * (k + 1 - i);
            int rest = (s * 10) % 11;
            d[k] = rest == 10 ? 0 : rest;
        }
        StringBuilder sb = new StringBuilder();
        for (int x : d) sb.append(x);
        return sb.toString();
    }
}
