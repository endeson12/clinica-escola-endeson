package br.edu.clinicaescola;
import org.junit.jupiter.api.Test;
import jakarta.servlet.http.Cookie;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class QueueDraftPrivacyTest extends EvaluationContractFixture {
 @Test void notaZeroPrivadaNaoVazaFeedback() throws Exception {
 var f=fixture(); grade(f,0,"Segredo QA",false).andExpect(status().isOk());
 queue(f.teacher().session(),f.activity(),"RASCUNHO").andExpect(jsonPath("$[0].nota").value(0)).andExpect(jsonPath("$[0].feedback").value("Segredo QA"));
 queue(f.student().session(),f.activity(),"PENDENTE").andExpect(jsonPath("$.length()").value(1)).andExpect(jsonPath("$[0].nota").doesNotExist()).andExpect(jsonPath("$[0].feedback").doesNotExist()).andExpect(jsonPath("$[0].conteudo").value("Resposta QA"));
 queue(f.student().session(),f.activity(),"RASCUNHO").andExpect(status().isForbidden());
 mvc.perform(get(B+"/boletim").cookie(f.student().session())).andExpect(jsonPath("$.length()").value(0));
 }
}
