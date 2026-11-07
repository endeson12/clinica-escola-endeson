package br.edu.clinicaescola.config;

import br.edu.clinicaescola.entity.Unidade;
import br.edu.clinicaescola.entity.Usuario;
import br.edu.clinicaescola.repository.UnidadeRepository;
import br.edu.clinicaescola.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cria (ou atualiza) os usuarios de demonstracao de forma idempotente.
 * As senhas vem SOMENTE de variaveis de ambiente e sao gravadas como hash BCrypt.
 *  - Administrador: acesso as duas unidades.
 *  - Recepcao: acesso apenas a UNINASSAU Joquei (usado para testar unidade nao autorizada).
 */
@Component
public class DemoUserInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoUserInitializer.class);

    private final UsuarioRepository usuarios;
    private final UnidadeRepository unidades;
    private final PasswordEncoder encoder;
    private final org.springframework.jdbc.core.JdbcTemplate jdbc;

    @Value("${app.demo.admin.email:}") private String adminEmail;
    @Value("${app.demo.admin.password:}") private String adminSenha;
    @Value("${app.demo.recepcao.email:}") private String recepEmail;
    @Value("${app.demo.recepcao.password:}") private String recepSenha;

    public DemoUserInitializer(UsuarioRepository usuarios, UnidadeRepository unidades, PasswordEncoder encoder,
                               org.springframework.jdbc.core.JdbcTemplate jdbc) {
        this.usuarios = usuarios;
        this.unidades = unidades;
        this.encoder = encoder;
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        garantir("Administrador Demonstrativo", adminEmail, adminSenha, unidades.findAll(), "ADMIN");
        unidades.findByCodigo("JOQUEI").ifPresent(j ->
                garantir("Recepção Jóquei (demo)", recepEmail, recepSenha, List.of(j), "RECEPCAO"));
    }

    private void garantir(String nome, String email, String senha, List<Unidade> acesso, String perfil) {
        if (email == null || email.isBlank() || senha == null || senha.isBlank()) {
            log.warn("Usuario demo '{}' nao configurado (variaveis de ambiente ausentes).", nome);
            return;
        }
        Usuario u = usuarios.findByEmailIgnoreCase(email.trim()).orElseGet(Usuario::new);
        u.setNome(nome);
        u.setEmail(email.trim().toLowerCase());
        if (u.getSenhaHash() == null || !encoder.matches(senha, u.getSenhaHash())) {
            u.setSenhaHash(encoder.encode(senha));
        }
        u.setAtivo(true);
        u.getUnidades().clear();
        u.getUnidades().addAll(acesso);
        usuarios.saveAndFlush(u);
        for (Unidade unidade : acesso) {
            jdbc.update("insert into academico_perfis(usuario_id,unidade_id,perfil) values(?,?,?) "
                    + "on conflict(usuario_id,unidade_id) do update set perfil=excluded.perfil",
                    u.getId(), unidade.getId(), perfil);
        }
        log.info("Usuario demo '{}' pronto com acesso a {} unidade(s).", nome, acesso.size());
    }
}
