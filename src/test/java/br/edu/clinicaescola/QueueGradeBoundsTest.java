package br.edu.clinicaescola;
import org.junit.jupiter.api.Test;
import jakarta.servlet.http.Cookie;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class QueueGradeBoundsTest extends EvaluationContractFixture {
 @Test void limitesDeNotaPreservamUltimaAvaliacao() throws Exception {
 var f=fixture(); grade(f,8.5,"Original",true).andExpect(status().isOk());
 for(Object invalid:List.of(-0.01,10.01,1.001)) grade(f,invalid,"Invasiva",false).andExpect(status().isBadRequest());
 queue(f.teacher().session(),f.activity(),"PUBLICADA").andExpect(jsonPath("$[0].nota").value(8.5)).andExpect(jsonPath("$[0].feedback").value("Original"));
 assertEquals(new java.math.BigDecimal("8.50"),jdbc.queryForObject("select nota from academico_entregas where id=?",java.math.BigDecimal.class,f.delivery()));
 }
}
