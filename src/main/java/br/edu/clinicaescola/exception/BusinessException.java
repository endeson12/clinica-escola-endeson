package br.edu.clinicaescola.exception;

import org.springframework.http.HttpStatus;

/** Erro de regra de negocio com status HTTP e (opcionalmente) o campo afetado. */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String campo;

    public BusinessException(HttpStatus status, String mensagem) {
        this(status, null, mensagem);
    }

    public BusinessException(HttpStatus status, String campo, String mensagem) {
        super(mensagem);
        this.status = status;
        this.campo = campo;
    }

    public HttpStatus getStatus() { return status; }
    public String getCampo() { return campo; }
}
