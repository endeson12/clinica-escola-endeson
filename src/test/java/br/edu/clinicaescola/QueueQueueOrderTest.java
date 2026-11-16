package br.edu.clinicaescola;
import org.junit.jupiter.api.Test;
import jakarta.servlet.http.Cookie;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class QueueQueueOrderTest extends EvaluationContractFixture {
 @Test void filaDaAtividadeOrdenaMaisRecentes() throws Exception {
 var f=fixture(); Actor other=actor(f.admin(),"ALUNO"); create(f.admin(),"/matriculas",Map.of("turmaId",f.turma(),"alunoId",other.id()));
 long e=create(other.session(),"/entregas",Map.of("atividadeId",f.activity(),"conteudo","Segunda resposta"));
 long at=activity(f.teacher().session(),f.turma(),"Outra atividade"); create(f.student().session(),"/entregas",Map.of("atividadeId",at,"conteudo","Outra"));
 queue(f.teacher().session(),f.activity(),"PENDENTE").andExpect(jsonPath("$.length()").value(2)).andExpect(jsonPath("$[0].id").value(e)).andExpect(jsonPath("$[1].id").value(f.delivery()));
 queue(f.student().session(),f.activity(),"PENDENTE").andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(f.delivery()));
 }
}
