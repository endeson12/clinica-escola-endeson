package br.edu.clinicaescola.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * Cria a conexao com o PostgreSQL a partir de variaveis de ambiente.
 * Aceita dois formatos:
 *  - JDBC_DATABASE_URL + DB_USERNAME + DB_PASSWORD (uso local / IntelliJ)
 *  - DATABASE_URL_UNPOOLED ou DATABASE_URL no formato postgres://usuario:senha@host/banco (Neon/Vercel)
 */
@Configuration
public class DataSourceConfig {

    @Bean
    public DataSource dataSource(Environment env) {
        HikariConfig cfg = new HikariConfig();
        String jdbcUrl = env.getProperty("JDBC_DATABASE_URL");
        if (jdbcUrl != null && !jdbcUrl.isBlank()) {
            cfg.setJdbcUrl(jdbcUrl);
            cfg.setUsername(env.getProperty("DB_USERNAME"));
            cfg.setPassword(env.getProperty("DB_PASSWORD"));
        } else {
            String url = firstNonBlank(env.getProperty("DATABASE_URL_UNPOOLED"), env.getProperty("DATABASE_URL"));
            if (url == null) {
                throw new IllegalStateException("Configure JDBC_DATABASE_URL ou DATABASE_URL (veja .env.example).");
            }
            applyPostgresUri(cfg, url);
        }
        cfg.setMaximumPoolSize(Integer.parseInt(env.getProperty("DB_POOL_SIZE", "3")));
        cfg.setMinimumIdle(0);
        cfg.setIdleTimeout(60_000);
        cfg.setMaxLifetime(300_000);
        cfg.setConnectionTimeout(15_000);
        cfg.setPoolName("clinica-pool");
        return new HikariDataSource(cfg);
    }

    static void applyPostgresUri(HikariConfig cfg, String raw) {
        URI uri = URI.create(raw.replaceFirst("^postgres(ql)?://", "http://"));
        String userInfo = uri.getRawUserInfo();
        if (userInfo != null) {
            String[] parts = userInfo.split(":", 2);
            cfg.setUsername(decode(parts[0]));
            if (parts.length > 1) cfg.setPassword(decode(parts[1]));
        }
        int port = uri.getPort() == -1 ? 5432 : uri.getPort();
        String query = uri.getRawQuery();
        StringBuilder q = new StringBuilder();
        if (query != null) {
            for (String p : query.split("&")) {
                // channel_binding e outros parametros do libpq nao sao entendidos pelo JDBC
                if (p.startsWith("sslmode=")) q.append(q.isEmpty() ? "" : "&").append(p);
            }
        }
        if (q.indexOf("sslmode=") < 0 && !"localhost".equals(uri.getHost()) && !"127.0.0.1".equals(uri.getHost())) {
            q.append(q.isEmpty() ? "" : "&").append("sslmode=require");
        }
        cfg.setJdbcUrl("jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath() + (q.isEmpty() ? "" : "?" + q));
    }

    private static String decode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }

    private static String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }
}
