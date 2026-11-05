package br.edu.clinicaescola.exception;

import java.util.Map;

/** Formato padrao de erro devolvido pela API. */
public record ApiError(int status, String message, Map<String, String> fieldErrors) {
}
