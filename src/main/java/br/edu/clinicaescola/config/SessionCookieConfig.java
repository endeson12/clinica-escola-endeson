package br.edu.clinicaescola.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/** Cookie da sessao: HttpOnly, SameSite=Lax e Secure em producao (HTTPS). */
@Configuration
public class SessionCookieConfig {

    @Bean
    public CookieSerializer cookieSerializer(@Value("${COOKIE_SECURE:false}") boolean secure) {
        DefaultCookieSerializer s = new DefaultCookieSerializer();
        s.setCookieName("CLINICA_SESSION");
        s.setUseHttpOnlyCookie(true);
        s.setSameSite("Lax");
        s.setUseSecureCookie(secure);
        s.setCookiePath("/");
        return s;
    }
}
