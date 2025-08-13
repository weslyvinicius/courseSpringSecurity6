package com.academy.springsecurity6full.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class PasswordEncoderConfig {

    /**
     * CODIFICADOR DE SENHAS
     * <p>
     * ATENÇÃO: NoOpPasswordEncoder é APENAS para desenvolvimento!
     * Ele NÃO criptografa as senhas, deixando-as em texto plano.
     * <p>
     * Em produção, use:
     * - BCryptPasswordEncoder (recomendado)
     * - Argon2PasswordEncoder
     * - SCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // NUNCA use em produção! Senhas ficam em texto plano!
        return NoOpPasswordEncoder.getInstance();

        // Para produção, use algo como:
        // return new BCryptPasswordEncoder();
    }
}
