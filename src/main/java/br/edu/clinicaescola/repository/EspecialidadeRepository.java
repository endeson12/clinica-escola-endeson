package br.edu.clinicaescola.repository;

import br.edu.clinicaescola.entity.Especialidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EspecialidadeRepository extends JpaRepository<Especialidade, Long> {
    List<Especialidade> findAllByOrderByNomeAsc();
    List<Especialidade> findByAtivaTrueOrderByNomeAsc();
    boolean existsByNomeIgnoreCase(String nome);
    long countByAtivaTrue();
}
