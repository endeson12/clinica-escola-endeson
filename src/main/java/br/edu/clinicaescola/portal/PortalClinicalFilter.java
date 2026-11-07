package br.edu.clinicaescola.portal;

import br.edu.clinicaescola.exception.BusinessException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Set;

/** Papéis acadêmicos não recebem acesso ao prontuário por estarem autenticados. */
public final class PortalClinicalFilter extends OncePerRequestFilter {
    private final PortalAccess access;
    private static final Set<String> CLINICA=Set.of("/api/pacientes","/api/especialidades","/api/atendimentos","/api/dashboard");
    public PortalClinicalFilter(PortalAccess access) { this.access=access; }
    @Override protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain) throws ServletException,IOException {
        String path=req.getRequestURI().substring(req.getContextPath().length());
        boolean clinica=CLINICA.stream().anyMatch(p->path.equals(p)||path.startsWith(p+"/"));
        var auth=SecurityContextHolder.getContext().getAuthentication();
        if(clinica&&auth!=null&&auth.isAuthenticated()&&!(auth instanceof AnonymousAuthenticationToken)) {
            try {
                var c=access.context(req,auth);
                if(!c.is("ADMIN","RECEPCAO")) {
                    res.setStatus(403); res.setContentType("application/json;charset=UTF-8");
                    res.getWriter().write("{\"status\":403,\"message\":\"Seu perfil não tem acesso aos dados clínicos.\",\"fieldErrors\":{}}"); return;
                }
            } catch(BusinessException ex) {
                res.setStatus(401); res.setContentType("application/json;charset=UTF-8");
                res.getWriter().write("{\"status\":401,\"message\":\"Sessão expirada. Faça login novamente.\",\"fieldErrors\":{}}"); return;
            }
        }
        chain.doFilter(req,res);
    }
}
