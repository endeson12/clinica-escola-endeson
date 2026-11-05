package br.edu.clinicaescola.security;

import br.edu.clinicaescola.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

/**
 * Le a unidade escolhida no login, guardada na sessao do SERVIDOR.
 * O navegador nunca informa a unidade nas requisicoes de cadastro/consulta.
 */
@Component
public class SessaoUnidade {

    public static final String ATRIBUTO = "UNIDADE_ID";

    public Long unidadeAtual(HttpServletRequest request) {
        HttpSession s = request.getSession(false);
        Object id = s == null ? null : s.getAttribute(ATRIBUTO);
        if (id == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Sessão expirada. Faça login novamente.");
        }
        return (Long) id;
    }
}
