package br.edu.clinicaescola;
import org.junit.jupiter.api.Test;
import jakarta.servlet.http.Cookie;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class QueueNormalizedStatusTest extends EvaluationContractFixture {
 @Test void normalizaSituacaoSemAlterarEscopo() throws Exception {
 var f=fixture();
 for(String v:List.of("", "  ","pendente"," PeNdEnTe ")) queue(f.teacher().session(),f.activity(),v).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].id").value(f.delivery()));
 queue(f.teacher().session(),f.activity(),"publicada").andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
 }
}
