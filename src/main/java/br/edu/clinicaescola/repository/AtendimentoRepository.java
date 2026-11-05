package br.edu.clinicaescola.repository;

import br.edu.clinicaescola.entity.Atendimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AtendimentoRepository extends JpaRepository<Atendimento, Long> {

    // join fetch evita consultas N+1 ao montar a listagem
    @Query("select a from Atendimento a join fetch a.paciente join fetch a.especialidade join fetch a.unidade "
            + "where a.unidade.id = :unidadeId order by a.dataHora desc")
    List<Atendimento> listarPorUnidade(@Param("unidadeId") Long unidadeId);

    long countByUnidadeId(Long unidadeId);
}
