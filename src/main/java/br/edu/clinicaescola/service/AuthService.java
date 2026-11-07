package br.edu.clinicaescola.service;

import br.edu.clinicaescola.dto.LoginRequest;
import br.edu.clinicaescola.dto.SessaoDto;
import br.edu.clinicaescola.dto.UnidadeDto;
import br.edu.clinicaescola.entity.Unidade;
import br.edu.clinicaescola.entity.Usuario;
import br.edu.clinicaescola.exception.BusinessException;
import br.edu.clinicaescola.repository.UnidadeRepository;
import br.edu.clinicaescola.repository.UsuarioRepository;
import br.edu.clinicaescola.security.SessaoUnidade;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final SecurityContextRepository contextRepo;
    private final UsuarioRepository usuarios;
    private final UnidadeRepository unidades;

    public AuthService(AuthenticationManager authManager, SecurityContextRepository contextRepo,
                       UsuarioRepository usuarios, UnidadeRepository unidades) {
        this.authManager = authManager;
        this.contextRepo = contextRepo;
        this.usuarios = usuarios;
        this.unidades = unidades;
    }

    @Transactional(readOnly = true)
    public SessaoDto login(LoginRequest req, HttpServletRequest request, HttpServletResponse response) {
        Authentication auth;
        try {
            auth = authManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(req.email().trim(), req.senha()));
        } catch (AuthenticationException e) {
            // mensagem generica: nao revela se o e-mail existe
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos.");
        }
        Usuario usuario = usuarios.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "E-mail ou senha incorretos."));
        Unidade unidade = unidades.findById(req.unidadeId())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "unidadeId", "Unidade inexistente."));
        if (!usuarios.temAcessoUnidade(usuario.getId(), unidade.getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "unidadeId",
                    "Seu usuário não está autorizado na unidade " + unidade.getNome() + ".");
        }

        // O login manual tambem precisa descartar o CSRF anterior a autenticacao.
        new org.springframework.security.web.csrf.CsrfAuthenticationStrategy(
                org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse())
                .onAuthentication(auth, request, response);

        // nova sessao (protecao contra fixacao de sessao) com o usuario e a unidade
        request.getSession(true);
        request.changeSessionId();
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        ctx.setAuthentication(auth);
        SecurityContextHolder.setContext(ctx);
        contextRepo.saveContext(ctx, request, response);
        request.getSession().setAttribute(SessaoUnidade.ATRIBUTO, unidade.getId());

        return new SessaoDto(usuario.getNome(), usuario.getEmail(), UnidadeDto.of(unidade));
    }

    @Transactional(readOnly = true)
    public SessaoDto sessaoAtual(String email, Long unidadeId) {
        Usuario usuario = usuarios.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Sessão inválida."));
        Unidade unidade = unidades.findById(unidadeId)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "Sessão inválida."));
        return new SessaoDto(usuario.getNome(), usuario.getEmail(), UnidadeDto.of(unidade));
    }
}
