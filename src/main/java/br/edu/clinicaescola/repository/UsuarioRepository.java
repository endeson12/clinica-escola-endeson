package br.edu.clinicaescola.repository;

import br.edu.clinicaescola.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    @Query("select count(u) > 0 from Usuario u join u.unidades un where u.id = :usuarioId and un.id = :unidadeId")
    boolean temAcessoUnidade(@Param("usuarioId") Long usuarioId, @Param("unidadeId") Long unidadeId);
}
