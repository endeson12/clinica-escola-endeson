package br.edu.clinicaescola.dto;


import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.assertThat;

class PacienteRequestValidationTest {
    @Test
    void nascimentoFuturoEEmailInvalidoSaoErrosDosCamposCorretos() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var request = new PacienteRequest("Pessoa fictícia", "fixture", "sem-arroba", null,
                    LocalDate.now().plusDays(2));
            assertThat(factory.getValidator().validate(request))
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactlyInAnyOrder("email", "dataNascimento");
        }
    }

    @Test
    void telefoneENascimentoPodemSerOmitidos() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var request = new PacienteRequest("Pessoa fictícia", "fixture", "pessoa@example.invalid", null, null);
            assertThat(factory.getValidator().validate(request)).isEmpty();
        }
    }
}
