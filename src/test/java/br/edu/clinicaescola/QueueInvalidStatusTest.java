package br.edu.clinicaescola;
import org.junit.jupiter.api.Test;
import jakarta.servlet.http.Cookie;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class QueueInvalidStatusTest extends EvaluationContractFixture {
 @Test void rejeitaSituacaoInvalidaSemMudarEntrega() throws Exception {
 var f=fixture();
 for(String v:List.of("INVALIDA","PUBLICADA OR 1=1","PENDENTE,RASCUNHO","<script>")) queue(f.admin(),f.activity(),v).andExpect(status().isBadRequest());
 assertEquals(1,jdbc.queryForObject("select count(*) from academico_entregas where id=? and nota is null and not publicada",Integer.class,f.delivery()));
 queue(f.teacher().session(),f.activity(),"PENDENTE").andExpect(jsonPath("$[0].id").value(f.delivery()));
 }
}
