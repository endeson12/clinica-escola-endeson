package br.edu.clinicaescola.exception;


import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerContractTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void erroDeCampoPreservaStatusMensagemECampo() {
        var result = handler.negocio(new BusinessException(HttpStatus.CONFLICT, "cpf", "Já cadastrado."));
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(result.getBody()).isEqualTo(new ApiError(409, "Já cadastrado.", Map.of("cpf", "Já cadastrado.")));
    }

    @Test
    void erroGeralNaoProduzChaveDeCampoFicticia() {
        var result = handler.negocio(new BusinessException(HttpStatus.NOT_FOUND, "Registro indisponível."));
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(result.getBody()).isEqualTo(new ApiError(404, "Registro indisponível.", Map.of()));
    }
}
