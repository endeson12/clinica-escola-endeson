package br.edu.clinicaescola.security;

import br.edu.clinicaescola.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

/** Revalida no banco o acesso das sessoes autenticadas antes de acessar a API. */
public final class SessaoAtivaFilter extends OncePerRequestFilter {
    private static final Set<String> PUBLICAS = Set.of(
            "/api/auth/login", "/api/auth/csrf", "/api/health", "/api/unidades");
    private final UsuarioRepository usuarios;

    public SessaoAtivaFilter(UsuarioRepository usuarios) {
        this.usuarios = usuarios;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        // MockMvc e despachos sem servletPath usam o URI relativo ao contexto.
        if (path.isEmpty()) path = request.getRequestURI().substring(request.getContextPath().length());
        return !path.startsWith("/api/") || PUBLICAS.contains(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            HttpSession session = request.getSession(false);
            Object unidadeId = session == null ? null : session.getAttribute(SessaoUnidade.ATRIBUTO);
            var usuario = usuarios.findByEmailIgnoreCase(auth.getName());
            boolean autorizado = unidadeId instanceof Long id
                    && usuario.isPresent() && usuario.get().isAtivo()
                    && usuarios.temAcessoUnidade(usuario.get().getId(), id);
            if (!autorizado) {
                if (session != null) session.invalidate();
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("{\"status\":401,\"message\":\"Sessão expirada. Faça login novamente.\",\"fieldErrors\":{}}");
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
