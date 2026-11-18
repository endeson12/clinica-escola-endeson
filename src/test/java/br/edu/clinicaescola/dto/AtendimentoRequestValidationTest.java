package br.edu.clinicaescola.dto;


import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class AtendimentoRequestValidationTest {
    @Test
    void identificadoresDataEStatusSaoObrigatorios() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var request = new AtendimentoRequest(null, null, null, " ", null);
            assertThat(factory.getValidator().validate(request))
                    .extracting(v -> v.getPropertyPath().toString())
                    .containsExactlyInAnyOrder("pacienteId", "especialidadeId", "dataHora", "status");
        }
    }

    @Test
    void observacoesAceitam1000CaracteresMasRejeitam1001() {
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var validator = factory.getValidator();
            var data = LocalDateTime.of(2026, 11, 18, 10, 0);
            assertThat(validator.validate(new AtendimentoRequest(1L, 2L, data, "AGENDADO", "x".repeat(1000))))
                    .isEmpty();
            assertThat(validator.validate(new AtendimentoRequest(1L, 2L, data, "AGENDADO", "x".repeat(1001))))
                    .extracting(v -> v.getPropertyPath().toString()).containsExactly("observacoes");
        }
    }
}
