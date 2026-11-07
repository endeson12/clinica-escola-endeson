package br.edu.clinicaescola.portal;

import br.edu.clinicaescola.exception.BusinessException;
import br.edu.clinicaescola.security.SessaoUnidade;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.*;
import static br.edu.clinicaescola.portal.PortalRepository.id;

@Component
public class PortalAccess {
    public static final Set<String> PERFIS=Set.of("ADMIN","COORDENACAO","PROFESSOR","ALUNO","FINANCEIRO","RECEPCAO");
    public record Context(long usuario,long unidade,String nome,String email,String perfil,String unidadeNome) {
        public boolean is(String... roles) { return Arrays.asList(roles).contains(perfil); }
    }
    private final PortalRepository db;
    private final SessaoUnidade sessao;
    public PortalAccess(PortalRepository db,SessaoUnidade sessao) { this.db=db; this.sessao=sessao; }
    public Context context(HttpServletRequest req,Authentication auth) {
        long unidade=sessao.unidadeAtual(req);
        Map<String,Object> row=db.one("""
          select u.id,u.nome,u.email,coalesce(p.perfil,'RECEPCAO') perfil,un.nome unidade_nome
          from usuarios u join usuarios_unidades uu on uu.usuario_id=u.id
          join unidades un on un.id=uu.unidade_id
          left join academico_perfis p on p.usuario_id=u.id and p.unidade_id=uu.unidade_id
          where lower(u.email)=lower(?) and u.ativo and uu.unidade_id=?
          """,auth.getName(),unidade);
        return new Context(id(row,"id"),unidade,row.get("nome").toString(),row.get("email").toString(),row.get("perfil").toString(),row.get("unidade_nome").toString());
    }
    public void require(Context c,String... perfis) {
        if(!c.is(perfis)) throw new BusinessException(HttpStatus.FORBIDDEN,"Seu perfil não tem permissão para esta operação.");
    }
    public Map<String,Object> turma(Context c,long turma) {
        require(c,"ADMIN","COORDENACAO","PROFESSOR","ALUNO");
        Map<String,Object> t=db.one("select * from academico_turmas where id=? and unidade_id=?",turma,c.unidade());
        if(c.is("ADMIN","COORDENACAO")) return t;
        if(c.is("PROFESSOR") && id(t,"professor_id")==c.usuario()) return t;
        if(c.is("ALUNO") && db.count("select count(*) from academico_matriculas where turma_id=? and aluno_id=? and unidade_id=? and status='ATIVA'",turma,c.usuario(),c.unidade())>0) return t;
        throw new BusinessException(HttpStatus.FORBIDDEN,"Você não tem vínculo ativo com esta turma.");
    }
    public void docente(Context c,long turma) { require(c,"ADMIN","COORDENACAO","PROFESSOR"); turma(c,turma); }
    public Map<String,Object> pessoa(Context c,long usuario,String perfil) {
        Map<String,Object> row=db.one("select u.id,u.nome,u.email,u.ativo,p.perfil from usuarios u join academico_perfis p on p.usuario_id=u.id where u.id=? and p.unidade_id=?",usuario,c.unidade());
        if(!Boolean.TRUE.equals(row.get("ativo")) || !perfil.equals(row.get("perfil")))
            throw new BusinessException(HttpStatus.BAD_REQUEST,"O usuário precisa estar ativo e ter o perfil "+perfil+" nesta unidade.");
        return row;
    }
    public long alunoConsulta(Context c,Long aluno) {
        if(c.is("ALUNO")) {
            if(aluno!=null && aluno!=c.usuario()) throw new BusinessException(HttpStatus.FORBIDDEN,"Você só pode consultar seus próprios registros.");
            return c.usuario();
        }
        return aluno==null?0:aluno;
    }
    /** Predicate SQL estático: apenas literais internos, sem entrada do cliente. */
    public String escopoTurma(Context c) {
        if(c.is("PROFESSOR")) return " and t.professor_id="+c.usuario();
        if(c.is("ALUNO")) return " and exists(select 1 from academico_matriculas mx where mx.turma_id=t.id and mx.aluno_id="+c.usuario()+" and mx.status='ATIVA')";
        return "";
    }
}
