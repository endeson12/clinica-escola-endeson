package br.edu.clinicaescola.portal;

import br.edu.clinicaescola.exception.BusinessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.Date;
import java.util.*;
import static br.edu.clinicaescola.portal.PortalRepository.id;
import static br.edu.clinicaescola.portal.PortalModels.*;

@Service
@Transactional(readOnly=true)
public class PortalStudentService {
    private final PortalRepository db; private final PortalAccess access;
    public PortalStudentService(PortalRepository db,PortalAccess access) { this.db=db; this.access=access; }
    public Map<String,Object> contexto(PortalAccess.Context c) {
        Map<String,Object> indicadores=new LinkedHashMap<>();
        if(c.is("ADMIN","COORDENACAO","PROFESSOR","ALUNO")) {
            indicadores.put("cursos",db.count("select count(*) from academico_cursos where unidade_id=?",c.unidade()));
            indicadores.put("disciplinas",db.count("select count(*) from academico_disciplinas where unidade_id=?",c.unidade()));
            indicadores.put("turmas",db.count("select count(*) from academico_turmas t where t.unidade_id=?"+access.escopoTurma(c),c.unidade()));
            String scope=access.escopoTurma(c)+(c.is("ALUNO")?" and m.aluno_id="+c.usuario():"");
            indicadores.put("matriculas",db.count("select count(*) from academico_matriculas m join academico_turmas t on t.id=m.turma_id where m.unidade_id=? and m.status='ATIVA'"+scope,c.unidade()));
        }
        if(c.is("FINANCEIRO")) indicadores.put("lancamentos",db.count("select count(*) from academico_financeiro where unidade_id=?",c.unidade()));
        return Map.of("usuario",Map.of("id",c.usuario(),"nome",c.nome(),"email",c.email(),"perfil",c.perfil()),
                "unidade",Map.of("id",c.unidade(),"nome",c.unidadeNome()),"indicadores",indicadores,
                "clinicaPermitida",c.is("ADMIN","RECEPCAO"),"documentosOficiais",false);
    }
    public List<Map<String,Object>> financeiro(PortalAccess.Context c,Long aluno) {
        access.require(c,"ADMIN","FINANCEIRO","ALUNO"); long usuario=access.alunoConsulta(c,aluno);
        return db.list("select f.*,u.nome aluno_nome from academico_financeiro f join usuarios u on u.id=f.aluno_id where f.unidade_id=?"+(usuario==0?"":" and f.aluno_id="+usuario)+" order by f.vencimento,f.id desc limit 1000",c.unidade());
    }
    @Transactional public Map<String,Object> lancar(PortalAccess.Context c,Lancamento r) {
        access.require(c,"ADMIN","FINANCEIRO"); access.pessoa(c,r.alunoId(),"ALUNO");
        Map<String,Object> row=db.one("insert into academico_financeiro(unidade_id,aluno_id,descricao,valor,vencimento) values(?,?,?,?,?) returning *",c.unidade(),r.alunoId(),r.descricao().trim(),r.valor(),Date.valueOf(r.vencimento()));
        db.audit(c,"financeiro",id(row,"id"),"LANCAR_DEMONSTRACAO"); return row;
    }
    @Transactional public Map<String,Object> baixa(PortalAccess.Context c,long registro) {
        access.require(c,"ADMIN","FINANCEIRO");
        db.one("select id from academico_financeiro where id=? and unidade_id=?",registro,c.unidade());
        Map<String,Object> row=db.one("update academico_financeiro set status='BAIXA_DEMONSTRATIVA' where id=? and unidade_id=? returning *",registro,c.unidade());
        db.audit(c,"financeiro",registro,"BAIXA_DEMONSTRATIVA"); return row;
    }
    public Map<String,Object> carteirinha(PortalAccess.Context c,Long aluno) {
        access.require(c,"ADMIN","COORDENACAO","ALUNO"); long usuario=access.alunoConsulta(c,aluno);
        if(usuario==0) throw new BusinessException(HttpStatus.BAD_REQUEST,"Selecione um aluno para consultar a carteirinha.");
        Map<String,Object> p=access.pessoa(c,usuario,"ALUNO");
        List<Map<String,Object>> cursos=db.list("""
            select distinct cu.id,cu.nome,cu.codigo from academico_matriculas m
            join academico_turmas t on t.id=m.turma_id join academico_disciplinas d on d.id=t.disciplina_id
            join academico_cursos cu on cu.id=d.curso_id
            where m.unidade_id=? and m.aluno_id=? and m.status='ATIVA' order by cu.nome
            """,c.unidade(),usuario);
        if(cursos.isEmpty()) throw new BusinessException(HttpStatus.NOT_FOUND,"A carteirinha exige matrícula ativa nesta unidade.");
        return Map.of("nome",p.get("nome"),"identificador","DEMO-"+c.unidade()+"-"+usuario,"unidade",c.unidadeNome(),"cursos",cursos,"oficial",false,"aviso","DEMONSTRAÇÃO — SEM VALIDADE OFICIAL. Não utilizar para meia-entrada ou identificação institucional.");
    }
    public List<Map<String,Object>> auditoria(PortalAccess.Context c) {
        access.require(c,"ADMIN");
        return db.list("select a.*,u.nome ator_nome from academico_auditoria a join usuarios u on u.id=a.ator_id where a.unidade_id=? order by a.id desc limit 200",c.unidade());
    }
}
