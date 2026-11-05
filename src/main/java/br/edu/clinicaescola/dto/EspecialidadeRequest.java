package br.edu.clinicaescola.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EspecialidadeRequest(
        @NotBlank(message = "Informe o nome da especialidade.") @Size(max = 100, message = "Nome muito longo.") String nome,
        @Size(max = 500, message = "Descrição muito longa.") String descricao,
        Boolean ativa) {
}
