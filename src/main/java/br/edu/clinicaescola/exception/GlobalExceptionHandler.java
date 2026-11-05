package br.edu.clinicaescola.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validacao(MethodArgumentNotValidException ex) {
        Map<String, String> erros = new LinkedHashMap<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            erros.putIfAbsent(fe.getField(), fe.getDefaultMessage());
        }
        return erro(HttpStatus.BAD_REQUEST, "Verifique os campos destacados.", erros);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiError> negocio(BusinessException ex) {
        Map<String, String> erros = ex.getCampo() == null ? Map.of() : Map.of(ex.getCampo(), ex.getMessage());
        return erro(ex.getStatus(), ex.getMessage(), erros);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> corpoInvalido(HttpMessageNotReadableException ex) {
        return erro(HttpStatus.BAD_REQUEST, "Dados enviados em formato inválido.", Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> integridade(DataIntegrityViolationException ex) {
        log.warn("Violacao de integridade: {}", ex.getMostSpecificCause().getMessage());
        return erro(HttpStatus.CONFLICT, "O registro conflita com dados já cadastrados.", Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> generico(Exception ex) {
        log.error("Erro inesperado", ex);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno. Tente novamente.", Map.of());
    }

    private ResponseEntity<ApiError> erro(HttpStatus status, String msg, Map<String, String> campos) {
        return ResponseEntity.status(status).body(new ApiError(status.value(), msg, campos));
    }
}
