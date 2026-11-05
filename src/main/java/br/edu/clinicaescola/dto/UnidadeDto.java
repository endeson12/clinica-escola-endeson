package br.edu.clinicaescola.dto;

import br.edu.clinicaescola.entity.Unidade;

public record UnidadeDto(Long id, String nome, String codigo) {
    public static UnidadeDto of(Unidade u) {
        return new UnidadeDto(u.getId(), u.getNome(), u.getCodigo());
    }
}
