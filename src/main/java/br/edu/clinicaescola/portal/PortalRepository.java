package br.edu.clinicaescola.portal;

import br.edu.clinicaescola.exception.BusinessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public class PortalRepository {
    private final JdbcTemplate jdbc;
    public PortalRepository(JdbcTemplate jdbc) { this.jdbc=jdbc; }
    public List<Map<String,Object>> list(String sql,Object... args) { return jdbc.queryForList(sql,args); }
    public Map<String,Object> one(String sql,Object... args) {
        List<Map<String,Object>> rows=list(sql,args);
        if(rows.isEmpty()) throw new BusinessException(HttpStatus.NOT_FOUND,"Registro não encontrado nesta unidade.");
        return rows.getFirst();
    }
    public long count(String sql,Object... args) { Long v=jdbc.queryForObject(sql,Long.class,args); return v==null?0:v; }
    public int update(String sql,Object... args) { return jdbc.update(sql,args); }
    public static long id(Map<String,Object> row,String key) { return ((Number)row.get(key)).longValue(); }
    public void audit(PortalAccess.Context c,String entidade,long registro,String acao) {
        update("insert into academico_auditoria(unidade_id,ator_id,entidade,registro_id,acao) values(?,?,?,?,?)",c.unidade(),c.usuario(),entidade,registro,acao);
    }
}
