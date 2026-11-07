package br.edu.clinicaescola.portal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static br.edu.clinicaescola.portal.PortalModels.*;

@RestController
@RequestMapping("/api/academico")
public class PortalController {
    private final PortalAccess access; private final PortalCatalogService catalog; private final PortalLearningService learning;
    private final PortalStudentService student; private final HttpServletRequest request;
    public PortalController(PortalAccess access,PortalCatalogService catalog,PortalLearningService learning,PortalStudentService student,HttpServletRequest request) {
        this.access=access; this.catalog=catalog; this.learning=learning; this.student=student; this.request=request;
    }
    private PortalAccess.Context c(Authentication a) { return access.context(request,a); }
    @GetMapping("/contexto") public Map<String,Object> contexto(Authentication a) { return student.contexto(c(a)); }
    @GetMapping("/pessoas") public List<Map<String,Object>> pessoas(Authentication a) { return catalog.pessoas(c(a)); }
    @PostMapping("/pessoas") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> pessoa(Authentication a,@Valid @RequestBody Pessoa r) { return catalog.criarPessoa(c(a),r); }
    @PatchMapping("/pessoas/{id}") public Map<String,Object> editarPessoa(Authentication a,@PathVariable long id,@Valid @RequestBody PessoaEdicao r) { return catalog.editarPessoa(c(a),id,r); }
    @GetMapping("/cursos") public List<Map<String,Object>> cursos(Authentication a) { return catalog.cursos(c(a)); }
    @PostMapping("/cursos") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> criarCurso(Authentication a,@Valid @RequestBody Curso r) { return catalog.curso(c(a),null,r); }
    @PutMapping("/cursos/{id}") public Map<String,Object> editarCurso(Authentication a,@PathVariable long id,@Valid @RequestBody Curso r) { return catalog.curso(c(a),id,r); }
    @GetMapping("/disciplinas") public List<Map<String,Object>> disciplinas(Authentication a) { return catalog.disciplinas(c(a)); }
    @PostMapping("/disciplinas") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> criarDisciplina(Authentication a,@Valid @RequestBody Disciplina r) { return catalog.disciplina(c(a),null,r); }
    @PutMapping("/disciplinas/{id}") public Map<String,Object> editarDisciplina(Authentication a,@PathVariable long id,@Valid @RequestBody Disciplina r) { return catalog.disciplina(c(a),id,r); }
    @GetMapping("/turmas") public List<Map<String,Object>> turmas(Authentication a) { return catalog.turmas(c(a)); }
    @PostMapping("/turmas") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> criarTurma(Authentication a,@Valid @RequestBody Turma r) { return catalog.turma(c(a),null,r); }
    @PutMapping("/turmas/{id}") public Map<String,Object> editarTurma(Authentication a,@PathVariable long id,@Valid @RequestBody Turma r) { return catalog.turma(c(a),id,r); }
    @GetMapping("/matriculas") public List<Map<String,Object>> matriculas(Authentication a) { return catalog.matriculas(c(a)); }
    @PostMapping("/matriculas") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> matricular(Authentication a,@Valid @RequestBody Matricula r) { return catalog.matricular(c(a),r); }
    @PostMapping("/matriculas/{id}/cancelar") public Map<String,Object> cancelar(Authentication a,@PathVariable long id) { return catalog.cancelar(c(a),id); }
    @GetMapping("/aulas") public List<Map<String,Object>> aulas(Authentication a,@RequestParam(required=false) Long turmaId) { return learning.aulas(c(a),turmaId); }
    @PostMapping("/aulas") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> criarAula(Authentication a,@Valid @RequestBody Aula r) { return learning.aula(c(a),null,r); }
    @PutMapping("/aulas/{id}") public Map<String,Object> editarAula(Authentication a,@PathVariable long id,@Valid @RequestBody Aula r) { return learning.aula(c(a),id,r); }
    @GetMapping("/atividades") public List<Map<String,Object>> atividades(Authentication a,@RequestParam(required=false) Long turmaId) { return learning.atividades(c(a),turmaId); }
    @PostMapping("/atividades") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> criarAtividade(Authentication a,@Valid @RequestBody Atividade r) { return learning.atividade(c(a),null,r); }
    @PutMapping("/atividades/{id}") public Map<String,Object> editarAtividade(Authentication a,@PathVariable long id,@Valid @RequestBody Atividade r) { return learning.atividade(c(a),id,r); }
    @GetMapping("/entregas") public List<Map<String,Object>> entregas(Authentication a,@RequestParam(required=false) Long atividadeId) { return learning.entregas(c(a),atividadeId); }
    @PostMapping("/entregas") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> entregar(Authentication a,@Valid @RequestBody Entrega r) { return learning.entregar(c(a),r); }
    @PutMapping("/entregas/{id}/avaliacao") public Map<String,Object> avaliar(Authentication a,@PathVariable long id,@Valid @RequestBody Avaliacao r) { return learning.avaliar(c(a),id,r); }
    @GetMapping("/boletim") public List<Map<String,Object>> boletim(Authentication a,@RequestParam(required=false) Long alunoId) { return learning.boletim(c(a),alunoId); }
    @GetMapping("/financeiro") public List<Map<String,Object>> financeiro(Authentication a,@RequestParam(required=false) Long alunoId) { return student.financeiro(c(a),alunoId); }
    @PostMapping("/financeiro") @ResponseStatus(HttpStatus.CREATED)
    public Map<String,Object> lancar(Authentication a,@Valid @RequestBody Lancamento r) { return student.lancar(c(a),r); }
    @PostMapping("/financeiro/{id}/baixa-demonstrativa") public Map<String,Object> baixa(Authentication a,@PathVariable long id) { return student.baixa(c(a),id); }
    @GetMapping("/carteirinha") public Map<String,Object> carteirinha(Authentication a,@RequestParam(required=false) Long alunoId) { return student.carteirinha(c(a),alunoId); }
    @GetMapping("/auditoria") public List<Map<String,Object>> auditoria(Authentication a) { return student.auditoria(c(a)); }
}
