package br.edu.clinicaescola.portal;

import br.edu.clinicaescola.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.util.*;
import static br.edu.clinicaescola.portal.PortalRepository.id;
import static br.edu.clinicaescola.portal.PortalModels.*;

@Service
@Transactional(readOnly=true)
public class PortalCatalogService {
    private final PortalRepository db; private final PortalAccess access; private final PasswordEncoder encoder;
    public PortalCatalogService(PortalRepository db,PortalAccess access,PasswordEncoder encoder) { this.db=db; this.access=access; this.encoder=encoder; }
    public List<Map<String,Object>> pessoas(PortalAccess.Context c) {
        access.require(c,"ADMIN","COORDENACAO","FINANCEIRO");
        return db.list("select u.id,u.nome,u.email,u.ativo,p.perfil from usuarios u join academico_perfis p on p.usuario_id=u.id where p.unidade_id=?"+(c.is("FINANCEIRO")?" and p.perfil='ALUNO'":"")+" order by u.nome limit 1000",c.unidade());
    }
    private void perfil(PortalAccess.Context c,String papel) {
        if(!PortalAccess.PERFIS.contains(papel)) throw new BusinessException(HttpStatus.BAD_REQUEST,"Perfil inválido.");
        if(c.is("COORDENACAO") && !Set.of("ALUNO","PROFESSOR").contains(papel))
            throw new BusinessException(HttpStatus.FORBIDDEN,"A coordenação só administra alunos e professores.");
    }
    @Transactional public Map<String,Object> criarPessoa(PortalAccess.Context c,Pessoa r) {
        access.require(c,"ADMIN","COORDENACAO"); perfil(c,r.perfil());
        if(r.senha().getBytes(StandardCharsets.UTF_8).length>72) throw new BusinessException(HttpStatus.BAD_REQUEST,"A senha excede o limite seguro de 72 bytes.");
        Map<String,Object> p=db.one("insert into usuarios(nome,email,senha_hash,ativo) values(?,?,?,true) returning id,nome,email,ativo",r.nome().trim(),r.email().trim().toLowerCase(Locale.ROOT),encoder.encode(r.senha()));
        long usuario=id(p,"id");
        db.update("insert into usuarios_unidades(usuario_id,unidade_id) values(?,?)",usuario,c.unidade());
        db.update("insert into academico_perfis(usuario_id,unidade_id,perfil) values(?,?,?)",usuario,c.unidade(),r.perfil());
        p.put("perfil",r.perfil()); db.audit(c,"pessoa",usuario,"CRIAR"); return p;
    }
    @Transactional public Map<String,Object> editarPessoa(PortalAccess.Context c,long usuario,PessoaEdicao r) {
        access.require(c,"ADMIN","COORDENACAO"); perfil(c,r.perfil());
        Map<String,Object> p=db.one("select u.id,u.ativo,p.perfil from usuarios u join academico_perfis p on p.usuario_id=u.id where u.id=? and p.unidade_id=? for update of u",usuario,c.unidade());
        if(c.is("COORDENACAO")&&!Set.of("ALUNO","PROFESSOR").contains(p.get("perfil"))) throw new BusinessException(HttpStatus.FORBIDDEN,"Perfil reservado à administração.");
        if(usuario==c.usuario()&&(!r.ativo()||!r.perfil().equals(c.perfil()))) throw new BusinessException(HttpStatus.CONFLICT,"Você não pode remover seu próprio acesso administrativo.");
        if(!Objects.equals(p.get("ativo"),r.ativo())&&db.count("select count(*) from usuarios_unidades where usuario_id=? and unidade_id<>?",usuario,c.unidade())>0) throw new BusinessException(HttpStatus.CONFLICT,"Contas multiunidade exigem administração central para desativação.");
        if(!r.perfil().equals(p.get("perfil"))&&(db.count("select count(*) from academico_matriculas where aluno_id=? and unidade_id=? and status='ATIVA'",usuario,c.unidade())>0||db.count("select count(*) from academico_turmas where professor_id=? and unidade_id=?",usuario,c.unidade())>0)) throw new BusinessException(HttpStatus.CONFLICT,"O perfil possui vínculos acadêmicos. Preserve o perfil e gerencie o acesso ativo.");
        db.update("update usuarios set nome=?,ativo=? where id=?",r.nome().trim(),r.ativo(),usuario);
        db.update("update academico_perfis set perfil=? where usuario_id=? and unidade_id=?",r.perfil(),usuario,c.unidade());
        db.audit(c,"pessoa",usuario,"ATUALIZAR_ACESSO");
        return db.one("select u.id,u.nome,u.email,u.ativo,p.perfil from usuarios u join academico_perfis p on p.usuario_id=u.id where u.id=? and p.unidade_id=?",usuario,c.unidade());
    }
    public List<Map<String,Object>> cursos(PortalAccess.Context c) {
        access.require(c,"ADMIN","COORDENACAO","PROFESSOR","ALUNO");
        return db.list("select * from academico_cursos where unidade_id=? order by nome limit 1000",c.unidade());
    }
    @Transactional public Map<String,Object> curso(PortalAccess.Context c,Long registro,Curso r) {
        access.require(c,"ADMIN","COORDENACAO");
        Map<String,Object> row;
        if(registro==null) row=db.one("insert into academico_cursos(unidade_id,codigo,nome,carga_horaria) values(?,?,?,?) returning *",c.unidade(),r.codigo().trim().toUpperCase(Locale.ROOT),r.nome().trim(),r.cargaHoraria());
        else row=db.one("update academico_cursos set codigo=?,nome=?,carga_horaria=? where id=? and unidade_id=? returning *",r.codigo().trim().toUpperCase(Locale.ROOT),r.nome().trim(),r.cargaHoraria(),registro,c.unidade());
        db.audit(c,"curso",id(row,"id"),registro==null?"CRIAR":"EDITAR"); return row;
    }
    public List<Map<String,Object>> disciplinas(PortalAccess.Context c) {
        access.require(c,"ADMIN","COORDENACAO","PROFESSOR","ALUNO");
        return db.list("select d.*,c.nome curso_nome from academico_disciplinas d join academico_cursos c on c.id=d.curso_id where d.unidade_id=? order by c.nome,d.nome limit 1000",c.unidade());
    }
    @Transactional public Map<String,Object> disciplina(PortalAccess.Context c,Long registro,Disciplina r) {
        access.require(c,"ADMIN","COORDENACAO"); db.one("select id from academico_cursos where id=? and unidade_id=? and ativo",r.cursoId(),c.unidade());
        Map<String,Object> row;
        if(registro==null) row=db.one("insert into academico_disciplinas(unidade_id,curso_id,codigo,nome,carga_horaria) values(?,?,?,?,?) returning *",c.unidade(),r.cursoId(),r.codigo().trim().toUpperCase(Locale.ROOT),r.nome().trim(),r.cargaHoraria());
        else row=db.one("update academico_disciplinas set curso_id=?,codigo=?,nome=?,carga_horaria=? where id=? and unidade_id=? returning *",r.cursoId(),r.codigo().trim().toUpperCase(Locale.ROOT),r.nome().trim(),r.cargaHoraria(),registro,c.unidade());
        db.audit(c,"disciplina",id(row,"id"),registro==null?"CRIAR":"EDITAR"); return row;
    }
    public List<Map<String,Object>> turmas(PortalAccess.Context c) {
        access.require(c,"ADMIN","COORDENACAO","PROFESSOR","ALUNO");
        return db.list("""
            select t.*,d.nome disciplina_nome,c.nome curso_nome,u.nome professor_nome,
            (select count(*) from academico_matriculas m where m.turma_id=t.id and m.status='ATIVA') alunos
            from academico_turmas t join academico_disciplinas d on d.id=t.disciplina_id
            join academico_cursos c on c.id=d.curso_id join usuarios u on u.id=t.professor_id
            where t.unidade_id=?
            """+access.escopoTurma(c)+" order by t.periodo desc,t.nome limit 1000",c.unidade());
    }
    @Transactional public Map<String,Object> turma(PortalAccess.Context c,Long registro,Turma r) {
        access.require(c,"ADMIN","COORDENACAO"); db.one("select id from academico_disciplinas where id=? and unidade_id=?",r.disciplinaId(),c.unidade());
        access.pessoa(c,r.professorId(),"PROFESSOR");
        Map<String,Object> row;
        if(registro==null) row=db.one("insert into academico_turmas(unidade_id,disciplina_id,professor_id,nome,periodo,vagas) values(?,?,?,?,?,?) returning *",c.unidade(),r.disciplinaId(),r.professorId(),r.nome().trim(),r.periodo().trim(),r.vagas());
        else {
            db.one("select id from academico_turmas where id=? and unidade_id=? for update",registro,c.unidade());
            if(db.count("select count(*) from academico_matriculas where turma_id=? and status='ATIVA'",registro)>r.vagas()) throw new BusinessException(HttpStatus.CONFLICT,"As vagas não podem ser menores que as matrículas ativas.");
            row=db.one("update academico_turmas set disciplina_id=?,professor_id=?,nome=?,periodo=?,vagas=? where id=? and unidade_id=? returning *",r.disciplinaId(),r.professorId(),r.nome().trim(),r.periodo().trim(),r.vagas(),registro,c.unidade());
        }
        db.audit(c,"turma",id(row,"id"),registro==null?"CRIAR":"EDITAR"); return row;
    }
    public List<Map<String,Object>> matriculas(PortalAccess.Context c) {
        access.require(c,"ADMIN","COORDENACAO","PROFESSOR","ALUNO");
        String scope=c.is("ALUNO")?" and m.aluno_id="+c.usuario():access.escopoTurma(c);
        return db.list("select m.*,u.nome aluno_nome,u.email aluno_email,t.nome turma_nome from academico_matriculas m join usuarios u on u.id=m.aluno_id join academico_turmas t on t.id=m.turma_id where m.unidade_id=?"+scope+" order by m.id desc limit 1000",c.unidade());
    }
    @Transactional public Map<String,Object> matricular(PortalAccess.Context c,Matricula r) {
        access.require(c,"ADMIN","COORDENACAO");
        Map<String,Object> t=db.one("select * from academico_turmas where id=? and unidade_id=? for update",r.turmaId(),c.unidade());
        access.pessoa(c,r.alunoId(),"ALUNO");
        if(db.count("select count(*) from academico_matriculas where turma_id=? and status='ATIVA'",r.turmaId())>=id(t,"vagas")) throw new BusinessException(HttpStatus.CONFLICT,"A turma não possui vagas disponíveis.");
        Map<String,Object> row=db.one("insert into academico_matriculas(unidade_id,turma_id,aluno_id) values(?,?,?) returning *",c.unidade(),r.turmaId(),r.alunoId());
        db.audit(c,"matricula",id(row,"id"),"MATRICULAR"); return row;
    }
    @Transactional public Map<String,Object> cancelar(PortalAccess.Context c,long registro) {
        access.require(c,"ADMIN","COORDENACAO");
        Map<String,Object> row=db.one("update academico_matriculas set status='CANCELADA' where id=? and unidade_id=? and status='ATIVA' returning *",registro,c.unidade());
        db.audit(c,"matricula",registro,"CANCELAR"); return row;
    }
}
