package br.edu.clinicaescola.controller;

import br.edu.clinicaescola.dto.LoginRequest;
import br.edu.clinicaescola.dto.SessaoDto;
import br.edu.clinicaescola.security.SessaoUnidade;
import br.edu.clinicaescola.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService auth;
    private final SessaoUnidade sessao;

    public AuthController(AuthService auth, SessaoUnidade sessao) {
        this.auth = auth;
        this.sessao = sessao;
    }

    /** Garante que o cookie XSRF-TOKEN exista antes do primeiro POST. */
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName());
    }

    @PostMapping("/login")
    public SessaoDto login(@Valid @RequestBody LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
        return auth.login(req, request, response);
    }

    @GetMapping("/me")
    public SessaoDto me(Authentication authentication, HttpServletRequest request) {
        return auth.sessaoAtual(authentication.getName(), sessao.unidadeAtual(request));
    }
    // POST /api/auth/logout e tratado pelo Spring Security (SecurityConfig)
}
