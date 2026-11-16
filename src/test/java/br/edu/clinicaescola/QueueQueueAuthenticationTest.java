package br.edu.clinicaescola;
import org.junit.jupiter.api.Test;
import jakarta.servlet.http.Cookie;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
class QueueQueueAuthenticationTest extends EvaluationContractFixture {
 @Test void filaExigeSessaoEPerfilAcademico() throws Exception {
 var f=fixture();
 mvc.perform(get(B+"/entregas").param("situacao","PENDENTE")).andExpect(status().isUnauthorized());
 for(String role:List.of("FINANCEIRO","RECEPCAO")) { Actor denied=actor(f.admin(),role); queue(denied.session(),f.activity(),"PENDENTE").andExpect(status().isForbidden()); }
 queue(f.teacher().session(),f.activity(),"PENDENTE").andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(f.delivery()));
 }
}
