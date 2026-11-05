package br.edu.clinicaescola.repository;

import br.edu.clinicaescola.entity.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {
    List<Paciente> findByUnidadeIdOrderByNomeCompletoAsc(Long unidadeId);
    boolean existsByUnidadeIdAndCpf(Long unidadeId, String cpf);
    Optional<Paciente> findByIdAndUnidadeId(Long id, Long unidadeId);
    long countByUnidadeId(Long unidadeId);
}
