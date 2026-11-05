package br.edu.clinicaescola.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class HealthController {

    private final JdbcTemplate jdbc;

    public HealthController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("aplicacao", "UP");
        try {
            Integer unidades = jdbc.queryForObject("select count(*) from unidades", Integer.class);
            String versao = jdbc.queryForObject(
                    "select max(version) from flyway_schema_history where success", String.class);
            body.put("banco", "UP");
            body.put("unidadesCadastradas", unidades);
            body.put("migracaoAtual", versao);
            return ResponseEntity.ok(body);
        } catch (Exception e) {
            body.put("banco", "DOWN");
            return ResponseEntity.status(503).body(body);
        }
    }
}
