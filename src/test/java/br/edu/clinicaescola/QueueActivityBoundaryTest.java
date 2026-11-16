package br.edu.clinicaescola;
import org.junit.jupiter.api.Test;
import jakarta.servlet.http.Cookie;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class QueueActivityBoundaryTest extends EvaluationContractFixture {
 @Test void atividadeAlheiaNaoPermiteEnumerarAvaliacoes() throws Exception {
 var f=fixture(); grade(f,7,"Privado",false).andExpect(status().isOk()); Actor stranger=actor(f.admin(),"PROFESSOR");
 queue(stranger.session(),f.activity(),"RASCUNHO").andExpect(status().isForbidden());
 Cookie otherUnit=login("admin.teste@clinica.local","REDENCAO"); queue(otherUnit,f.activity(),"RASCUNHO").andExpect(status().isNotFound());
 queue(f.admin(),Long.MAX_VALUE,"PUBLICADA").andExpect(status().isNotFound());
 queue(f.teacher().session(),f.activity(),"RASCUNHO").andExpect(jsonPath("$[0].feedback").value("Privado"));
 }
}
