package br.edu.clinicaescola.portal;

import br.edu.clinicaescola.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.net.URI;
import java.time.*;
import java.util.*;
import static br.edu.clinicaescola.portal.PortalRepository.id;

/** Session-bound self-service; no external institution or payment integration. */
@RestController
@RequestMapping("/api/academico")
@Transactional
public class PortalServicesController {
 private final PortalAccess access; private final PortalRepository db; private final HttpServletRequest request;
 public PortalServicesController(PortalAccess access,PortalRepository db,HttpServletRequest request){this.access=access;this.db=db;this.request=request;}
 private PortalAccess.Context ctx(Authentication a){return access.context(request,a);}
 private void bad(String message){throw new BusinessException(HttpStatus.BAD_REQUEST,message);}
 private void conflict(String message){throw new BusinessException(HttpStatus.CONFLICT,message);}
 private static String text(String s){return s==null?"":s.strip();}
 private static final Set<String> TYPES=Set.of("DOCUMENTOS","SEGUNDA_CHAMADA","DISPENSA","MUDANCA_CURSO","TRANSFERENCIA","MUDANCA_TURMA","MUDANCA_TURNO","INCLUSAO_DISCIPLINA","CERTIFICADOS","ATENDIMENTO","OUTROS");
 public record Profile(@Size(max=25) String telefone,@Size(max=12) String cep,@Size(max=240) String endereco,
  @Size(max=20) String numero,@Size(max=120) String complemento,@Size(max=100) String bairro,
  @Size(max=100) String cidade,@Pattern(regexp="^$|^[A-Z]{2}$") String estado,@Size(max=160) String escola,
  @Min(1900) @Max(2100) Integer anoConclusao){}
 public record RequestNew(@NotBlank @Size(max=30) String tipo,@NotBlank @Size(max=160) String assunto,@NotBlank @Size(max=4000) String mensagem){}
 public record Answer(@NotBlank @Size(max=4000) String resposta){}
 public record Complement(@Positive Long alunoId,@NotBlank @Size(max=160) String titulo,@NotNull @PastOrPresent LocalDate data,
  @NotNull @DecimalMin("0.25") @DecimalMax("1000") BigDecimal horas,@Size(max=2000) String comprovanteUrl){}
 public record Review(@NotBlank String status,@NotNull @DecimalMin("0") @DecimalMax("1000") BigDecimal horasAprovadas,@NotBlank @Size(max=2000) String parecer){}
 public record Schedule(@NotNull @Positive Long turmaId,@NotNull @Min(1) @Max(7) Integer dia,
  @NotNull LocalTime inicio,@NotNull LocalTime fim,@Size(max=100) String sala){}

 @GetMapping("/perfil") public Map<String,Object> profile(Authentication a){return profile(ctx(a));}
 private Map<String,Object> profile(PortalAccess.Context c){
  Map<String,Object> r=new LinkedHashMap<>();r.put("nome",c.nome());r.put("email",c.email());r.put("perfil",c.perfil());r.put("unidade",c.unidadeNome());
  for(String key:List.of("telefone","cep","endereco","numero","complemento","bairro","cidade","estado","escola"))r.put(key,"");r.put("ano_conclusao",null);
  List<Map<String,Object>> data=db.list("select telefone,cep,endereco,numero,complemento,bairro,cidade,estado,escola,ano_conclusao from academico_dados_pessoais where usuario_id=? and unidade_id=?",c.usuario(),c.unidade());
  if(!data.isEmpty())r.putAll(data.getFirst());return r;
 }
 @PutMapping("/perfil") public Map<String,Object> saveProfile(Authentication a,@Valid @RequestBody Profile r){
  var c=ctx(a);db.update("""
   insert into academico_dados_pessoais(usuario_id,unidade_id,telefone,cep,endereco,numero,complemento,bairro,cidade,estado,escola,ano_conclusao)
   values(?,?,?,?,?,?,?,?,?,?,?,?) on conflict(usuario_id,unidade_id) do update set telefone=excluded.telefone,cep=excluded.cep,
   endereco=excluded.endereco,numero=excluded.numero,complemento=excluded.complemento,bairro=excluded.bairro,cidade=excluded.cidade,
   estado=excluded.estado,escola=excluded.escola,ano_conclusao=excluded.ano_conclusao,atualizado_em=now()
   """,c.usuario(),c.unidade(),text(r.telefone()),text(r.cep()),text(r.endereco()),text(r.numero()),text(r.complemento()),text(r.bairro()),text(r.cidade()),text(r.estado()),text(r.escola()),r.anoConclusao());
  db.audit(c,"DADOS_PESSOAIS",c.usuario(),"ATUALIZAR");return profile(c);
 }
 @GetMapping("/solicitacoes") public List<Map<String,Object>> requests(Authentication a){var c=ctx(a);return db.list("""
  select s.id,s.usuario_id,u.nome solicitante,s.tipo,s.assunto,s.mensagem,s.status,s.resposta,s.criado_em,s.respondido_em
  from academico_solicitacoes s join usuarios u on u.id=s.usuario_id where s.unidade_id=?
  """+(c.is("ADMIN","COORDENACAO")?"":" and s.usuario_id="+c.usuario())+" order by s.id desc limit 1000",c.unidade());}
 @PostMapping("/solicitacoes") @ResponseStatus(HttpStatus.CREATED) public Map<String,Object> createRequest(Authentication a,@Valid @RequestBody RequestNew r){
  var c=ctx(a);if(!TYPES.contains(r.tipo()))bad("Tipo de solicitação inválido.");
  var row=db.one("insert into academico_solicitacoes(unidade_id,usuario_id,tipo,assunto,mensagem) values(?,?,?,?,?) returning id,status",c.unidade(),c.usuario(),r.tipo(),text(r.assunto()),text(r.mensagem()));db.audit(c,"SOLICITACAO",id(row,"id"),"CRIAR");return row;
 }
 @PutMapping("/solicitacoes/{id}/resposta") public Map<String,Object> answer(Authentication a,@PathVariable long id,@Valid @RequestBody Answer r){
  var c=ctx(a);access.require(c,"ADMIN","COORDENACAO");var row=db.one("select status from academico_solicitacoes where id=? and unidade_id=? for update",id,c.unidade());if(!"ABERTA".equals(row.get("status")))conflict("Somente solicitações abertas podem receber resposta.");
  db.update("update academico_solicitacoes set resposta=?,status='RESPONDIDA',respondido_por=?,respondido_em=now() where id=? and unidade_id=?",text(r.resposta()),c.usuario(),id,c.unidade());db.audit(c,"SOLICITACAO",id,"RESPONDER");return Map.of("id",id,"status","RESPONDIDA");
 }
 @PostMapping("/solicitacoes/{id}/cancelar") public Map<String,Object> cancelRequest(Authentication a,@PathVariable long id){
  var c=ctx(a);var row=db.one("select status from academico_solicitacoes where id=? and unidade_id=? and usuario_id=? for update",id,c.unidade(),c.usuario());if(!"ABERTA".equals(row.get("status")))conflict("Somente solicitações abertas podem ser canceladas.");
  db.update("update academico_solicitacoes set status='CANCELADA' where id=? and unidade_id=? and usuario_id=?",id,c.unidade(),c.usuario());db.audit(c,"SOLICITACAO",id,"CANCELAR");return Map.of("id",id,"status","CANCELADA");
 }
 @GetMapping("/complementares") public List<Map<String,Object>> complements(Authentication a){
  var c=ctx(a);access.require(c,"ADMIN","COORDENACAO","ALUNO");return db.list("""
   select s.id,s.aluno_id,u.nome aluno_nome,s.titulo,s.data,s.horas,s.comprovante_url,s.status,s.horas_aprovadas,s.parecer,s.criado_em
   from academico_complementares s join usuarios u on u.id=s.aluno_id where s.unidade_id=?
   """+(c.is("ALUNO")?" and s.aluno_id="+c.usuario():"")+" order by s.id desc limit 1000",c.unidade());
 }
 @PostMapping("/complementares") @ResponseStatus(HttpStatus.CREATED) public Map<String,Object> createComplement(Authentication a,@Valid @RequestBody Complement r){
  var c=ctx(a);access.require(c,"ADMIN","COORDENACAO","ALUNO");long student=access.alunoConsulta(c,r.alunoId());if(student==0)bad("Selecione um aluno.");access.pessoa(c,student,"ALUNO");
  String url=text(r.comprovanteUrl());if(!url.isEmpty())try{URI u=URI.create(url);if(!"https".equalsIgnoreCase(u.getScheme())||u.getHost()==null||u.getUserInfo()!=null)bad("Use uma referência HTTPS sem credenciais.");}catch(IllegalArgumentException e){bad("Referência HTTPS inválida.");}
  var row=db.one("insert into academico_complementares(unidade_id,aluno_id,titulo,data,horas,comprovante_url) values(?,?,?,?,?,?) returning id,status",c.unidade(),student,text(r.titulo()),r.data(),r.horas(),url.isEmpty()?null:url);db.audit(c,"COMPLEMENTAR",id(row,"id"),"SUBMETER");return row;
 }
 @PutMapping("/complementares/{id}/avaliacao") public Map<String,Object> review(Authentication a,@PathVariable long id,@Valid @RequestBody Review r){
  var c=ctx(a);access.require(c,"ADMIN","COORDENACAO");var row=db.one("select status,horas from academico_complementares where id=? and unidade_id=? for update",id,c.unidade());
  if(!"PENDENTE".equals(row.get("status")))conflict("Esta atividade já foi avaliada.");
  boolean approved="APROVADA".equals(r.status());if(!approved&&!"REPROVADA".equals(r.status()))bad("Status de avaliação inválido.");
  if(r.horasAprovadas().compareTo((BigDecimal)row.get("horas"))>0||(approved&&r.horasAprovadas().signum()<=0)||(!approved&&r.horasAprovadas().signum()!=0))bad("Horas aprovadas devem respeitar a carga enviada e o parecer.");
  db.update("update academico_complementares set status=?,horas_aprovadas=?,parecer=?,avaliado_por=?,avaliado_em=now() where id=? and unidade_id=?",r.status(),r.horasAprovadas(),text(r.parecer()),c.usuario(),id,c.unidade());db.audit(c,"COMPLEMENTAR",id,"AVALIAR");return Map.of("id",id,"status",r.status());
 }
 @GetMapping("/horarios") public List<Map<String,Object>> schedules(Authentication a){
  var c=ctx(a);access.require(c,"ADMIN","COORDENACAO","PROFESSOR","ALUNO");return db.list("""
   select h.id,h.turma_id,h.dia,h.inicio,h.fim,h.sala,t.nome turma_nome,t.periodo,d.nome disciplina_nome,u.nome professor_nome
   from academico_horarios h join academico_turmas t on t.id=h.turma_id and t.unidade_id=h.unidade_id
   join academico_disciplinas d on d.id=t.disciplina_id join usuarios u on u.id=t.professor_id
   where h.unidade_id=? and h.ativo
   """+access.escopoTurma(c)+" order by h.dia,h.inicio,h.id limit 1000",c.unidade());
 }
 @PostMapping("/horarios") @ResponseStatus(HttpStatus.CREATED) public Map<String,Object> schedule(Authentication a,@Valid @RequestBody Schedule r){
  var c=ctx(a);access.require(c,"ADMIN","COORDENACAO");access.turma(c,r.turmaId());if(!r.fim().isAfter(r.inicio()))bad("O término deve ser posterior ao início.");
  var row=db.one("insert into academico_horarios(unidade_id,turma_id,dia,inicio,fim,sala) values(?,?,?,?,?,?) returning id",c.unidade(),r.turmaId(),r.dia(),r.inicio(),r.fim(),text(r.sala()));db.audit(c,"HORARIO",id(row,"id"),"CRIAR");return row;
 }
 @PostMapping("/horarios/{id}/cancelar") public Map<String,Object> cancelSchedule(Authentication a,@PathVariable long id){
  var c=ctx(a);access.require(c,"ADMIN","COORDENACAO");db.one("select id from academico_horarios where id=? and unidade_id=?",id,c.unidade());db.update("update academico_horarios set ativo=false where id=? and unidade_id=?",id,c.unidade());db.audit(c,"HORARIO",id,"CANCELAR");return Map.of("id",id,"ativo",false);
 }
}
