package br.edu.clinicaescola.service;

import br.edu.clinicaescola.dto.PacienteRequest;
import br.edu.clinicaescola.dto.PacienteResponse;
import br.edu.clinicaescola.entity.Paciente;
import br.edu.clinicaescola.exception.BusinessException;
import br.edu.clinicaescola.repository.PacienteRepository;
import br.edu.clinicaescola.repository.UnidadeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PacienteService {

    private final PacienteRepository pacientes;
    private final UnidadeRepository unidades;

    public PacienteService(PacienteRepository pacientes, UnidadeRepository unidades) {
        this.pacientes = pacientes;
        this.unidades = unidades;
    }

    @Transactional(readOnly = true)
    public List<PacienteResponse> listar(Long unidadeId) {
        return pacientes.findByUnidadeIdOrderByNomeCompletoAsc(unidadeId).stream().map(PacienteResponse::of).toList();
    }

    @Transactional
    public PacienteResponse cadastrar(PacienteRequest req, Long unidadeId) {
        String cpf = CpfValidator.normalizar(req.cpf());
        if (!CpfValidator.isValido(cpf)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "cpf", "CPF inválido.");
        }
        if (pacientes.existsByUnidadeIdAndCpf(unidadeId, cpf)) {
            throw new BusinessException(HttpStatus.CONFLICT, "cpf", "Já existe um paciente com este CPF nesta unidade.");
        }
        String telefone = req.telefone() == null ? null : req.telefone().replaceAll("\\D", "");
        if (telefone != null && !telefone.isEmpty() && (telefone.length() < 10 || telefone.length() > 11)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "telefone", "Telefone deve ter DDD + número (10 ou 11 dígitos).");
        }
        Paciente p = new Paciente();
        p.setUnidade(unidades.getReferenceById(unidadeId));
        p.setNomeCompleto(req.nomeCompleto().trim().replaceAll("\\s+", " "));
        p.setCpf(cpf);
        p.setEmail(req.email().trim().toLowerCase());
        p.setTelefone(telefone == null || telefone.isEmpty() ? null : telefone);
        p.setDataNascimento(req.dataNascimento());
        return PacienteResponse.of(pacientes.saveAndFlush(p));
    }
}
