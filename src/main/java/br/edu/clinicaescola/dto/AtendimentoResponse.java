package br.edu.clinicaescola.dto;

import br.edu.clinicaescola.entity.Atendimento;

import java.time.LocalDateTime;

public record AtendimentoResponse(Long id, Long pacienteId, String paciente, Long especialidadeId,
                                  String especialidade, LocalDateTime dataHora, String status,
                                  String observacoes, String unidade) {
    public static AtendimentoResponse of(Atendimento a) {
        return new AtendimentoResponse(a.getId(), a.getPaciente().getId(), a.getPaciente().getNomeCompleto(),
                a.getEspecialidade().getId(), a.getEspecialidade().getNome(), a.getDataHora(),
                a.getStatus().name(), a.getObservacoes(), a.getUnidade().getNome());
    }
}
