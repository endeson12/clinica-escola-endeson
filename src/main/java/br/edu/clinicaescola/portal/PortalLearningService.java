package br.edu.clinicaescola.portal;

import br.edu.clinicaescola.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import static br.edu.clinicaescola.portal.PortalRepository.id;
import static br.edu.clinicaescola.portal.PortalModels.*;

@Service
@Transactional(readOnly=true)
public class PortalLearningService {
    private final PortalRepository db; private final PortalAccess access;
    public PortalLearningService(PortalRepository db,PortalAccess access) { this.db=db; this.access=access; }
    private String filtro(PortalAccess.Context c,Long turma) {
        access.require(c,"ADMIN","COORDENACAO","PROFESSOR","ALUNO");
        if(turma!=null) access.turma(c,turma);
        return access.escopoTurma(c)+(turma==null?"":" and t.id="+turma);
    }
    private String url(String valor) {
        if(valor==null||valor.isBlank()) return null;
        try {
            URI u=URI.create(valor.trim());
            if(!"https".equalsIgnoreCase(u.getScheme())||u.getHost()==null||u.getUserInfo()!=null) throw new IllegalArgumentException();
            return u.toASCIIString();
        } catch(IllegalArgumentException ex) { throw new BusinessException(HttpStatus.BAD_REQUEST,"materialUrl","Use um endereço HTTPS completo, sem credenciais."); }
    }
    public List<Map<String,Object>> aulas(PortalAccess.Context c,Long turma) {
        String scope=filtro(c,turma);
        return db.list("select a.*,t.nome turma_nome from academico_aulas a join academico_turmas t on t.id=a.turma_id where a.unidade_id=?"+scope+" order by a.id desc limit 1000",c.unidade());
    }
    @Transactional public Map<String,Object> aula(PortalAccess.Context c,Long registro,Aula r) {
        access.docente(c,r.turmaId()); String material=url(r.materialUrl());
        Map<String,Object> row;
        if(registro==null) row=db.one("insert into academico_aulas(unidade_id,turma_id,titulo,conteudo,material_url) values(?,?,?,?,?) returning *",c.unidade(),r.turmaId(),r.titulo().trim(),r.conteudo().trim(),material);
        else row=db.one("update academico_aulas set titulo=?,conteudo=?,material_url=? where id=? and unidade_id=? and turma_id=? returning *",r.titulo().trim(),r.conteudo().trim(),material,registro,c.unidade(),r.turmaId());
        db.audit(c,"aula",id(row,"id"),registro==null?"PUBLICAR":"EDITAR"); return row;
    }
    public List<Map<String,Object>> atividades(PortalAccess.Context c,Long turma) {
        String scope=filtro(c,turma);
        return db.list("""
            select a.*,t.nome turma_nome, exists(select 1 from academico_entregas e where e.atividade_id=a.id and e.aluno_id=?) entregue
            from academico_atividades a join academico_turmas t on t.id=a.turma_id where a.unidade_id=?
            """+scope+" order by a.prazo,a.id desc limit 1000",c.usuario(),c.unidade());
    }
    @Transactional public Map<String,Object> atividade(PortalAccess.Context c,Long registro,Atividade r) {
        access.docente(c,r.turmaId()); Map<String,Object> row;
        if(registro==null) row=db.one("insert into academico_atividades(unidade_id,turma_id,titulo,instrucoes,prazo) values(?,?,?,?,?) returning *",c.unidade(),r.turmaId(),r.titulo().trim(),r.instrucoes().trim(),Timestamp.from(r.prazo()));
        else {
            db.one("select id from academico_atividades where id=? and unidade_id=? and turma_id=? for update",registro,c.unidade(),r.turmaId());
            if(db.count("select count(*) from academico_entregas where atividade_id=?",registro)>0) throw new BusinessException(HttpStatus.CONFLICT,"Atividades com entregas não podem ter seu enunciado ou prazo alterado.");
            row=db.one("update academico_atividades set titulo=?,instrucoes=?,prazo=? where id=? and unidade_id=? and turma_id=? returning *",r.titulo().trim(),r.instrucoes().trim(),Timestamp.from(r.prazo()),registro,c.unidade(),r.turmaId());
        }
        db.audit(c,"atividade",id(row,"id"),registro==null?"PUBLICAR":"EDITAR"); return row;
    }
    public List<Map<String,Object>> entregas(PortalAccess.Context c,Long atividade) {
        access.require(c,"ADMIN","COORDENACAO","PROFESSOR","ALUNO");
        if(atividade!=null) {
            Map<String,Object> a=db.one("select turma_id from academico_atividades where id=? and unidade_id=?",atividade,c.unidade());
            access.turma(c,id(a,"turma_id"));
        }
        String scope=access.escopoTurma(c)+(c.is("ALUNO")?" and e.aluno_id="+c.usuario():"")+(atividade==null?"":" and a.id="+atividade);
        return db.list("""
            select e.id,e.unidade_id,e.atividade_id,e.aluno_id,e.conteudo,e.enviado_em,e.publicada,
            case when e.publicada or ? then e.nota else null end nota,
            case when e.publicada or ? then e.feedback else null end feedback,
            a.titulo atividade_titulo,t.nome turma_nome,u.nome aluno_nome
            from academico_entregas e join academico_atividades a on a.id=e.atividade_id
            join academico_turmas t on t.id=a.turma_id join usuarios u on u.id=e.aluno_id
            where e.unidade_id=?
            """+scope+" order by e.id desc limit 1000",!c.is("ALUNO"),!c.is("ALUNO"),c.unidade());
    }
    @Transactional public Map<String,Object> entregar(PortalAccess.Context c,Entrega r) {
        access.require(c,"ALUNO");
        Map<String,Object> a=db.one("select * from academico_atividades where id=? and unidade_id=? for update",r.atividadeId(),c.unidade());
        access.turma(c,id(a,"turma_id"));
        Object limite=a.get("prazo");
        Instant prazo=limite instanceof Timestamp ts?ts.toInstant():((java.time.OffsetDateTime)limite).toInstant();
        if(Instant.now().isAfter(prazo)) throw new BusinessException(HttpStatus.CONFLICT,"O prazo desta atividade já foi encerrado.");
        Map<String,Object> row=db.one("insert into academico_entregas(unidade_id,atividade_id,aluno_id,conteudo) values(?,?,?,?) returning id,atividade_id,aluno_id,conteudo,enviado_em",c.unidade(),r.atividadeId(),c.usuario(),r.conteudo().trim());
        db.audit(c,"entrega",id(row,"id"),"ENVIAR_FINAL"); return row;
    }
    @Transactional public Map<String,Object> avaliar(PortalAccess.Context c,long entrega,Avaliacao r) {
        access.require(c,"ADMIN","COORDENACAO","PROFESSOR");
        Map<String,Object> row=db.one("select e.id,a.turma_id from academico_entregas e join academico_atividades a on a.id=e.atividade_id where e.id=? and e.unidade_id=? for update of e",entrega,c.unidade());
        access.docente(c,id(row,"turma_id"));
        row=db.one("update academico_entregas set nota=?,feedback=?,publicada=?,avaliado_por=?,avaliado_em=now() where id=? and unidade_id=? returning id,nota,feedback,publicada,avaliado_em",r.nota(),r.feedback()==null?"":r.feedback().trim(),r.publicada(),c.usuario(),entrega,c.unidade());
        db.audit(c,"entrega",entrega,r.publicada()?"PUBLICAR_AVALIACAO":"SALVAR_AVALIACAO"); return row;
    }
    public List<Map<String,Object>> boletim(PortalAccess.Context c,Long aluno) {
        access.require(c,"ADMIN","COORDENACAO","PROFESSOR","ALUNO");
        long usuario=access.alunoConsulta(c,aluno);
        String scope=(usuario==0?"":" and e.aluno_id="+usuario)+(c.is("PROFESSOR")?access.escopoTurma(c):"");
        return db.list("""
            select e.id,e.aluno_id,u.nome aluno_nome,a.titulo atividade_titulo,d.nome disciplina_nome,
            t.nome turma_nome,t.periodo,e.nota,e.feedback,e.avaliado_em,false oficial
            from academico_entregas e join academico_atividades a on a.id=e.atividade_id
            join academico_turmas t on t.id=a.turma_id join academico_disciplinas d on d.id=t.disciplina_id
            join usuarios u on u.id=e.aluno_id where e.unidade_id=? and e.publicada
            """+scope+" order by t.periodo desc,d.nome,a.titulo limit 1000",c.unidade());
    }
}
