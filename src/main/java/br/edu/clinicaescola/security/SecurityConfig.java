package br.edu.clinicaescola.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(UsuarioDetailsService uds, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(uds);
        provider.setPasswordEncoder(encoder);
        return new ProviderManager(provider);
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, SecurityContextRepository repo,
                                           br.edu.clinicaescola.repository.UsuarioRepository usuarios,
                                           br.edu.clinicaescola.portal.PortalAccess portalAccess) throws Exception {
        // CSRF: token em cookie legivel pelo JS (XSRF-TOKEN) e enviado no cabecalho X-XSRF-TOKEN
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName(null); // gera o cookie ja na primeira requisicao

        http
            .csrf(c -> c
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(csrfHandler))
            .securityContext(s -> s.securityContextRepository(repo))
            .addFilterBefore(new SessaoAtivaFilter(usuarios),
                    org.springframework.security.web.access.intercept.AuthorizationFilter.class)
            .addFilterBefore(new br.edu.clinicaescola.portal.PortalClinicalFilter(portalAccess),
                    org.springframework.security.web.access.intercept.AuthorizationFilter.class)
            .authorizeHttpRequests(a -> a
                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/img/**", "/favicon.ico", "/error").permitAll()
                .requestMatchers("/api/auth/login", "/api/auth/csrf", "/api/health", "/api/unidades").permitAll()
                .anyRequest().authenticated())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((req, res, ex) -> {
                    if (req.getRequestURI().startsWith("/api/")) {
                        json(res, HttpServletResponse.SC_UNAUTHORIZED, "Faça login para continuar.");
                    } else {
                        res.sendRedirect("/index.html");
                    }
                })
                .accessDeniedHandler((req, res, ex) ->
                        json(res, HttpServletResponse.SC_FORBIDDEN, "Acesso negado. Atualize a página e tente novamente.")))
            .logout(l -> l
                .logoutUrl("/api/auth/logout")
                .invalidateHttpSession(true)
                .deleteCookies("CLINICA_SESSION")
                .logoutSuccessHandler((req, res, auth) -> res.setStatus(HttpServletResponse.SC_NO_CONTENT)))
            .formLogin(f -> f.disable())
            .httpBasic(b -> b.disable())
            .headers(h -> h.frameOptions(fo -> fo.deny()));
        return http.build();
    }

    private static void json(HttpServletResponse res, int status, String msg) throws java.io.IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding("UTF-8");
        res.getWriter().write("{\"status\":" + status + ",\"message\":\"" + msg + "\",\"fieldErrors\":{}}");
    }
}
