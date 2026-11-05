package br.edu.clinicaescola.controller;

import br.edu.clinicaescola.dto.DashboardDto;
import br.edu.clinicaescola.repository.AtendimentoRepository;
import br.edu.clinicaescola.repository.EspecialidadeRepository;
import br.edu.clinicaescola.repository.PacienteRepository;
import br.edu.clinicaescola.security.SessaoUnidade;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final PacienteRepository pacientes;
    private final AtendimentoRepository atendimentos;
    private final EspecialidadeRepository especialidades;
    private final SessaoUnidade sessao;

    public DashboardController(PacienteRepository pacientes, AtendimentoRepository atendimentos,
                               EspecialidadeRepository especialidades, SessaoUnidade sessao) {
        this.pacientes = pacientes;
        this.atendimentos = atendimentos;
        this.especialidades = especialidades;
        this.sessao = sessao;
    }

    @GetMapping
    public DashboardDto resumo(HttpServletRequest request) {
        Long unidade = sessao.unidadeAtual(request);
        return new DashboardDto(pacientes.countByUnidadeId(unidade), atendimentos.countByUnidadeId(unidade),
                especialidades.countByAtivaTrue());
    }
}
