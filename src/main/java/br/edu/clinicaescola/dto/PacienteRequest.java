package br.edu.clinicaescola.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PacienteRequest(
        @NotBlank(message = "Informe o nome completo.") @Size(max = 160, message = "Nome muito longo.") String nomeCompleto,
        @NotBlank(message = "Informe o CPF.") String cpf,
        @NotBlank(message = "Informe o e-mail.") @Email(message = "E-mail inválido.") @Size(max = 160) String email,
        @Size(max = 20, message = "Telefone muito longo.") String telefone,
        @PastOrPresent(message = "A data de nascimento não pode ser futura.") LocalDate dataNascimento) {
}
