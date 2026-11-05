package br.edu.clinicaescola.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AtendimentoRequest(
        @NotNull(message = "Selecione o paciente.") Long pacienteId,
        @NotNull(message = "Selecione a especialidade.") Long especialidadeId,
        @NotNull(message = "Informe a data e o horário.") LocalDateTime dataHora,
        @NotBlank(message = "Selecione o status.") String status,
        @Size(max = 1000, message = "Observações muito longas.") String observacoes) {
}
