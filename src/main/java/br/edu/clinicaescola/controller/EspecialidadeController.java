package br.edu.clinicaescola.controller;

import br.edu.clinicaescola.dto.EspecialidadeRequest;
import br.edu.clinicaescola.dto.EspecialidadeResponse;
import br.edu.clinicaescola.service.EspecialidadeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/especialidades")
public class EspecialidadeController {

    private final EspecialidadeService service;

    public EspecialidadeController(EspecialidadeService service) {
        this.service = service;
    }

    @GetMapping
    public List<EspecialidadeResponse> listar(@RequestParam(defaultValue = "false") boolean ativas) {
        return service.listar(ativas);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EspecialidadeResponse cadastrar(@Valid @RequestBody EspecialidadeRequest req) {
        return service.cadastrar(req);
    }
}
