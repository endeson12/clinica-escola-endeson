package br.edu.clinicaescola.controller;

import br.edu.clinicaescola.dto.PacienteRequest;
import br.edu.clinicaescola.dto.PacienteResponse;
import br.edu.clinicaescola.security.SessaoUnidade;
import br.edu.clinicaescola.service.PacienteService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final PacienteService service;
    private final SessaoUnidade sessao;

    public PacienteController(PacienteService service, SessaoUnidade sessao) {
        this.service = service;
        this.sessao = sessao;
    }

    @GetMapping
    public List<PacienteResponse> listar(HttpServletRequest request) {
        return service.listar(sessao.unidadeAtual(request));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PacienteResponse cadastrar(@Valid @RequestBody PacienteRequest req, HttpServletRequest request) {
        return service.cadastrar(req, sessao.unidadeAtual(request));
    }
}
