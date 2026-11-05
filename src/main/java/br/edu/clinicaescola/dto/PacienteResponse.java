package br.edu.clinicaescola.dto;

import br.edu.clinicaescola.entity.Paciente;

import java.time.LocalDate;

public record PacienteResponse(Long id, String nomeCompleto, String cpf, String email,
                               String telefone, LocalDate dataNascimento) {
    public static PacienteResponse of(Paciente p) {
        return new PacienteResponse(p.getId(), p.getNomeCompleto(), formatarCpf(p.getCpf()), p.getEmail(),
                p.getTelefone(), p.getDataNascimento());
    }

    static String formatarCpf(String c) {
        if (c == null || c.length() != 11) return c;
        return c.substring(0, 3) + "." + c.substring(3, 6) + "." + c.substring(6, 9) + "-" + c.substring(9);
    }
}
