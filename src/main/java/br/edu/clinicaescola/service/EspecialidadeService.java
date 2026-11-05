package br.edu.clinicaescola.service;

import br.edu.clinicaescola.dto.EspecialidadeRequest;
import br.edu.clinicaescola.dto.EspecialidadeResponse;
import br.edu.clinicaescola.entity.Especialidade;
import br.edu.clinicaescola.exception.BusinessException;
import br.edu.clinicaescola.repository.EspecialidadeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EspecialidadeService {

    private final EspecialidadeRepository especialidades;

    public EspecialidadeService(EspecialidadeRepository especialidades) {
        this.especialidades = especialidades;
    }

    @Transactional(readOnly = true)
    public List<EspecialidadeResponse> listar(boolean somenteAtivas) {
        var lista = somenteAtivas ? especialidades.findByAtivaTrueOrderByNomeAsc() : especialidades.findAllByOrderByNomeAsc();
        return lista.stream().map(EspecialidadeResponse::of).toList();
    }

    @Transactional
    public EspecialidadeResponse cadastrar(EspecialidadeRequest req) {
        String nome = req.nome().trim().replaceAll("\\s+", " ");
        if (especialidades.existsByNomeIgnoreCase(nome)) {
            throw new BusinessException(HttpStatus.CONFLICT, "nome", "Esta especialidade já está cadastrada.");
        }
        Especialidade e = new Especialidade();
        e.setNome(nome);
        e.setDescricao(req.descricao() == null || req.descricao().isBlank() ? null : req.descricao().trim());
        e.setAtiva(req.ativa() == null || req.ativa());
        return EspecialidadeResponse.of(especialidades.saveAndFlush(e));
    }
}
