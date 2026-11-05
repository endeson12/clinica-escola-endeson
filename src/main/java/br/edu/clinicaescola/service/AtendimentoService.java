package br.edu.clinicaescola.service;

import br.edu.clinicaescola.dto.AtendimentoRequest;
import br.edu.clinicaescola.dto.AtendimentoResponse;
import br.edu.clinicaescola.entity.Atendimento;
import br.edu.clinicaescola.entity.Especialidade;
import br.edu.clinicaescola.entity.Paciente;
import br.edu.clinicaescola.entity.StatusAtendimento;
import br.edu.clinicaescola.exception.BusinessException;
import br.edu.clinicaescola.repository.AtendimentoRepository;
import br.edu.clinicaescola.repository.EspecialidadeRepository;
import br.edu.clinicaescola.repository.PacienteRepository;
import br.edu.clinicaescola.repository.UnidadeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class AtendimentoService {

    private final AtendimentoRepository atendimentos;
    private final PacienteRepository pacientes;
    private final EspecialidadeRepository especialidades;
    private final UnidadeRepository unidades;

    public AtendimentoService(AtendimentoRepository atendimentos, PacienteRepository pacientes,
                              EspecialidadeRepository especialidades, UnidadeRepository unidades) {
        this.atendimentos = atendimentos;
        this.pacientes = pacientes;
        this.especialidades = especialidades;
        this.unidades = unidades;
    }

    @Transactional(readOnly = true)
    public List<AtendimentoResponse> listar(Long unidadeId) {
        return atendimentos.listarPorUnidade(unidadeId).stream().map(AtendimentoResponse::of).toList();
    }

    @Transactional
    public AtendimentoResponse cadastrar(AtendimentoRequest req, Long unidadeId) {
        StatusAtendimento status;
        try {
            status = StatusAtendimento.valueOf(req.status().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "status", "Status inválido. Use Agendado, Realizado ou Cancelado.");
        }
        // o paciente precisa existir E pertencer a unidade da sessao
        Paciente paciente = pacientes.findByIdAndUnidadeId(req.pacienteId(), unidadeId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "pacienteId",
                        "Paciente não encontrado nesta unidade."));
        Especialidade esp = especialidades.findById(req.especialidadeId())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "especialidadeId",
                        "Especialidade não encontrada."));
        if (!esp.isAtiva()) {
            throw new BusinessException(HttpStatus.UNPROCESSABLE_ENTITY, "especialidadeId",
                    "A especialidade selecionada está inativa.");
        }
        Atendimento a = new Atendimento();
        a.setUnidade(unidades.getReferenceById(unidadeId));
        a.setPaciente(paciente);
        a.setEspecialidade(esp);
        a.setDataHora(req.dataHora().withSecond(0).withNano(0));
        a.setStatus(status);
        a.setObservacoes(req.observacoes() == null || req.observacoes().isBlank() ? null : req.observacoes().trim());
        Atendimento salvo = atendimentos.saveAndFlush(a);
        return atendimentos.listarPorUnidade(unidadeId).stream()
                .filter(x -> x.getId().equals(salvo.getId())).findFirst()
                .map(AtendimentoResponse::of).orElseThrow();
    }
}
