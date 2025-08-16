package com.academy.springsecurity6full.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
public class TokenCustomizerConfig {

    /**
     * Token Customizer que mapeia escopos solicitados pelo cliente
     * para authorities efetivas do usuário
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer() {
        return (context) -> {
            if ("access_token".equals(context.getTokenType().getValue())) {

                // 1. Obter escopos solicitados pelo clientex
                Set<String> requestedScopes = context.getAuthorizedScopes();

                // 2. Obter authorities do usuário autenticado
                Set<String> userAuthorities = context.getPrincipal().getAuthorities()
                        .stream()
                        .map(authority -> authority.getAuthority())
                        .collect(Collectors.toSet());

                // 3. Adicionar claims no token
                context.getClaims().claim("authorities", userAuthorities);
                context.getClaims().claim("scope", requestedScopes);
                context.getClaims().claim("username", context.getPrincipal().getName());

                // 4. Informações do usuário para OIDC
                if (requestedScopes.contains("profile")) {
                    context.getClaims().claim("name", context.getPrincipal().getName());
                    context.getClaims().claim("preferred_username", context.getPrincipal().getName());
                }
            }
        };
    }


}
