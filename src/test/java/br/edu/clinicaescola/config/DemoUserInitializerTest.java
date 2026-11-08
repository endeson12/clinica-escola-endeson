package br.edu.clinicaescola.config;

import br.edu.clinicaescola.repository.UnidadeRepository;
import br.edu.clinicaescola.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.*;

class DemoUserInitializerTest {
    @Test
    void bootstrapDesativadoNaoConsultaNemModificaUsuarios() throws Exception {
        var usuarios=mock(UsuarioRepository.class);var unidades=mock(UnidadeRepository.class);
        var encoder=mock(PasswordEncoder.class);var jdbc=mock(JdbcTemplate.class);
        var initializer=new DemoUserInitializer(usuarios,unidades,encoder,jdbc);
        ReflectionTestUtils.setField(initializer,"demoEnabled",false);
        ReflectionTestUtils.setField(initializer,"adminEmail","never.seed@example.invalid");
        ReflectionTestUtils.setField(initializer,"adminSenha","Isolated-Test-Only#321");
        initializer.run(mock(ApplicationArguments.class));
        verifyNoInteractions(usuarios,unidades,encoder,jdbc);
    }
}
