package br.edu.clinicaescola;
import org.junit.jupiter.api.Test;
import jakarta.servlet.http.Cookie;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class QueueDeadlineTest extends EvaluationContractFixture {
 @Test void prazoEncerradoNaoGravaEntrega() throws Exception {
 var f=fixture(); long at=activity(f.teacher().session(),f.turma(),"Encerrada");
 jdbc.update("update academico_atividades set prazo='2000-01-01T00:00:00Z' where id=?",at);
 send(f.student().session(),post(B+"/entregas"),Map.of("atividadeId",at,"conteudo","Fora do prazo")).andExpect(status().isConflict());
 assertEquals(0,jdbc.queryForObject("select count(*) from academico_entregas where atividade_id=?",Integer.class,at));
 queue(f.teacher().session(),at,"PENDENTE").andExpect(jsonPath("$.length()").value(0));
 }
}
