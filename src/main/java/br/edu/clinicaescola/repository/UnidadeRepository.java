package br.edu.clinicaescola.repository;

import br.edu.clinicaescola.entity.Unidade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UnidadeRepository extends JpaRepository<Unidade, Long> {
    List<Unidade> findAllByOrderByNomeAsc();
    Optional<Unidade> findByCodigo(String codigo);
}
