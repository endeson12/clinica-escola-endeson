package br.edu.clinicaescola;
import org.junit.jupiter.api.Test;
import jakarta.servlet.http.Cookie;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class QueueDuplicateDeliveryTest extends EvaluationContractFixture {
 @Test void entregaFinalNaoPodeSerDuplicada() throws Exception {
 var f=fixture();
 send(f.student().session(),post(B+"/entregas"),Map.of("atividadeId",f.activity(),"conteudo","Sobrescrever")).andExpect(status().isConflict());
 assertEquals(1,jdbc.queryForObject("select count(*) from academico_entregas where atividade_id=? and aluno_id=?",Integer.class,f.activity(),f.student().id()));
 queue(f.student().session(),f.activity(),"").andExpect(jsonPath("$[0].conteudo").value("Resposta QA"));
 }
}
