package net.gedeon.Collab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // TODO: Configurer Spring Security avec JWT
    // TODO: Configurer les endpoints publics (/api/auth/**)
    // TODO: Configurer les endpoints protégés (tous les autres)
}
