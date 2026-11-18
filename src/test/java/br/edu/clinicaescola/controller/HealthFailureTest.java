package br.edu.clinicaescola.controller;


import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class HealthFailureTest {
    @Test
    void falhaNaConsultaDeMigracaoRetorna503SemDadosParciais() {
        JdbcTemplate db = mock(JdbcTemplate.class);
        when(db.queryForObject(anyString(), eq(Integer.class))).thenReturn(2);
        when(db.queryForObject(anyString(), eq(String.class)))
                .thenThrow(new DataAccessResourceFailureException("diagnóstico interno fictício"));

        var resposta = new HealthController(db).health();

        assertThat(resposta.getStatusCode().value()).isEqualTo(503);
        assertThat(resposta.getBody()).containsExactlyInAnyOrderEntriesOf(
                Map.of("aplicacao", "UP", "banco", "DOWN"));
    }
}
