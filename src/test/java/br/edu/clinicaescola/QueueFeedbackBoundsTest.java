package br.edu.clinicaescola;
import org.junit.jupiter.api.Test;
import jakarta.servlet.http.Cookie;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class QueueFeedbackBoundsTest extends EvaluationContractFixture {
 @Test void feedbackNoLimiteERejeicaoSemTruncar() throws Exception {
 var f=fixture(); String max="x".repeat(4000);
 grade(f,10,max,true).andExpect(status().isOk()); grade(f,1,max+"x",false).andExpect(status().isBadRequest());
 queue(f.student().session(),f.activity(),"PUBLICADA").andExpect(jsonPath("$[0].feedback").value(max)).andExpect(jsonPath("$[0].nota").value(10));
 assertEquals(4000,jdbc.queryForObject("select length(feedback) from academico_entregas where id=?",Integer.class,f.delivery()));
 }
}
