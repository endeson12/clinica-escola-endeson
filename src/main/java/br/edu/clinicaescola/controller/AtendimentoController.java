package br.edu.clinicaescola.controller;

import br.edu.clinicaescola.dto.AtendimentoRequest;
import br.edu.clinicaescola.dto.AtendimentoResponse;
import br.edu.clinicaescola.security.SessaoUnidade;
import br.edu.clinicaescola.service.AtendimentoService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/atendimentos")
public class AtendimentoController {

    private final AtendimentoService service;
    private final SessaoUnidade sessao;

    public AtendimentoController(AtendimentoService service, SessaoUnidade sessao) {
        this.service = service;
        this.sessao = sessao;
    }

    @GetMapping
    public List<AtendimentoResponse> listar(HttpServletRequest request) {
        return service.listar(sessao.unidadeAtual(request));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AtendimentoResponse cadastrar(@Valid @RequestBody AtendimentoRequest req, HttpServletRequest request) {
        return service.cadastrar(req, sessao.unidadeAtual(request));
    }
}
