package br.edu.clinicaescola.config;


import com.zaxxer.hikari.HikariConfig;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class PostgresUriTest {
    @Test
    void hostRemotoSemModoExplicitoExigeTls() {
        var cfg = new HikariConfig();
        DataSourceConfig.applyPostgresUri(cfg, "postgres://fixture@banco.example.invalid/clinica?channel_binding=require");
        assertThat(cfg.getUsername()).isEqualTo("fixture");
        assertThat(cfg.getJdbcUrl()).isEqualTo("jdbc:postgresql://banco.example.invalid:5432/clinica?sslmode=require");
    }

    @Test
    void configuracaoLocalPreservaPortaEModoExplicitamenteInformados() {
        var cfg = new HikariConfig();
        DataSourceConfig.applyPostgresUri(cfg, "postgresql://127.0.0.1:5544/teste?sslmode=disable&application_name=fixture");
        assertThat(cfg.getJdbcUrl()).isEqualTo("jdbc:postgresql://127.0.0.1:5544/teste?sslmode=disable");
    }
}
