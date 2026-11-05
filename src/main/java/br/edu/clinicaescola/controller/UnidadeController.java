package br.edu.clinicaescola.controller;

import br.edu.clinicaescola.dto.UnidadeDto;
import br.edu.clinicaescola.repository.UnidadeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/unidades")
public class UnidadeController {

    private final UnidadeRepository unidades;

    public UnidadeController(UnidadeRepository unidades) {
        this.unidades = unidades;
    }

    @GetMapping
    public List<UnidadeDto> listar() {
        return unidades.findAllByOrderByNomeAsc().stream().map(UnidadeDto::of).toList();
    }
}
