package br.edu.clinicaescola.dto;

import br.edu.clinicaescola.entity.Especialidade;

public record EspecialidadeResponse(Long id, String nome, String descricao, boolean ativa) {
    public static EspecialidadeResponse of(Especialidade e) {
        return new EspecialidadeResponse(e.getId(), e.getNome(), e.getDescricao(), e.isAtiva());
    }
}
