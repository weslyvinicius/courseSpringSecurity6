package com.academy.springsecurity6full.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;

import java.util.*;
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

                // 1. Obter escopos solicitados pelo cliente
                Set<String> requestedScopes = context.getAuthorizedScopes();

                // 2. Obter authorities do usuário autenticado
                Set<String> userAuthorities = context.getPrincipal().getAuthorities()
                        .stream()
                        .map(authority -> authority.getAuthority())
                        .collect(Collectors.toSet());

                // 3. Fazer mapeamento: Escopos → Authorities permitidas
                Set<String> effectivePermissions = mapScopesToAuthorities(requestedScopes, userAuthorities);

                // 4. Adicionar claims no token
                context.getClaims().claim("authorities", effectivePermissions);
                context.getClaims().claim("scope", requestedScopes);
                context.getClaims().claim("username", context.getPrincipal().getName());

                // 5. Claims adicionais para debug (remover em produção)
                context.getClaims().claim("debug_user_authorities", userAuthorities);
                context.getClaims().claim("debug_requested_scopes", requestedScopes);

                // 6. Informações do usuário para OIDC
                if (requestedScopes.contains("profile")) {
                    context.getClaims().claim("name", context.getPrincipal().getName());
                    context.getClaims().claim("preferred_username", context.getPrincipal().getName());
                }
            }
        };
    }

    /**
     * MAPEAMENTO CENTRAL: Escopos → Authorities
     *
     * Esta é a lógica principal que define quais authorities do usuário
     * são necessárias para cada escopo solicitado pelo cliente.
     */
    private Set<String> mapScopesToAuthorities(Set<String> requestedScopes, Set<String> userAuthorities) {

        Set<String> effectivePermissions = new HashSet<>();

        // MAPEAMENTO DE ESCOPOS PARA AUTHORITIES REQUERIDAS
        Map<String, Set<String>> scopeToAuthorityMap = Map.of(
                // Escopos de Employee
                "employee.read", Set.of("READ_EMPLOYEE"),
                "employee.write", Set.of("CREATE_EMPLOYEE", "UPDATE_EMPLOYEE"),
                "employee.delete", Set.of("DELETE_EMPLOYEE"),

                // Escopos de Report
                "report.read", Set.of("READ_REPORT"),
                "report.write", Set.of("CREATE_REPORT", "UPDATE_REPORT"),
                "report.delete", Set.of("DELETE_REPORT"),

                // Escopo Admin (requer ROLE_ADMIN)
                "admin", Set.of("ROLE_ADMIN"),

                // Escopos de API (para client_credentials)
                "api.read", Set.of("API_READ"),
                "api.write", Set.of("API_WRITE"),
                "system.integration", Set.of("SYSTEM_INTEGRATION")
        );

        // Para cada escopo solicitado pelo cliente
        for (String requestedScope : requestedScopes) {

            // Pular escopos OIDC (não precisam de authorities específicas)
            if (isOidcScope(requestedScope)) {
                continue;
            }

            Set<String> requiredAuthoritiesForScope = scopeToAuthorityMap.get(requestedScope);

            if (requiredAuthoritiesForScope != null) {
                // Verificar se o usuário tem TODAS as authorities necessárias para este escopo
                boolean hasAllRequiredAuthorities = userAuthorities.containsAll(requiredAuthoritiesForScope);

                if (hasAllRequiredAuthorities) {
                    // Usuário tem as authorities necessárias, adicionar as permissions efetivas
                    effectivePermissions.addAll(requiredAuthoritiesForScope);

                    // Para escopo admin, adicionar todas as authorities de admin
                    if ("admin".equals(requestedScope) && userAuthorities.contains("ROLE_ADMIN")) {
                        // Admin tem acesso a tudo
                        effectivePermissions.addAll(
                                userAuthorities.stream()
                                        .filter(auth -> !auth.startsWith("ROLE_"))
                                        .collect(Collectors.toSet())
                        );
                        effectivePermissions.add("ROLE_ADMIN");
                    }
                }
            }
        }

        // Sempre incluir as roles do usuário se ele tem as permissions correspondentes
        if (!effectivePermissions.isEmpty()) {
            userAuthorities.stream()
                    .filter(auth -> auth.startsWith("ROLE_"))
                    .forEach(effectivePermissions::add);
        }

        return effectivePermissions;
    }

    /**
     * Verifica se é um escopo padrão do OpenID Connect
     */
    private boolean isOidcScope(String scope) {
        return Set.of("openid", "profile", "email", "address", "phone").contains(scope);
    }
}

/**
 * EXEMPLOS DE COMO O MAPEAMENTO FUNCIONA:
 *
 * CENÁRIO 1: Mobile App + John (Employee)
 * - Cliente solicita: ["openid", "profile", "employee.read", "report.read"]
 * - John tem: ["ROLE_EMPLOYEE", "READ_EMPLOYEE"]
 * - Resultado: ["READ_EMPLOYEE", "ROLE_EMPLOYEE"]
 *   (report.read não incluído pois John não tem READ_REPORT)
 *
 * CENÁRIO 2: Dashboard + Mary (Manager)
 * - Cliente solicita: ["openid", "profile", "employee.read", "employee.write", "report.read", "report.write"]
 * - Mary tem: ["ROLE_MANAGER", "READ_EMPLOYEE", "CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "READ_REPORT"]
 * - Resultado: ["READ_EMPLOYEE", "CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "READ_REPORT", "ROLE_MANAGER"]
 *   (report.write não incluído pois Mary não tem CREATE_REPORT/UPDATE_REPORT)
 *
 * CENÁRIO 3: Web App + Susan (Admin)
 * - Cliente solicita: ["openid", "profile", "employee.read", "employee.write", "employee.delete", "admin"]
 * - Susan tem: ["ROLE_ADMIN", "READ_EMPLOYEE", "CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "DELETE_EMPLOYEE", ...]
 * - Resultado: TODAS as authorities de Susan (acesso completo)
 *
 * CENÁRIO 4: Reports App + Mary (Manager)
 * - Cliente solicita: ["openid", "profile", "report.read", "report.write"]
 * - Mary tem: ["ROLE_MANAGER", "READ_EMPLOYEE", "CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "READ_REPORT"]
 * - Resultado: ["READ_REPORT", "ROLE_MANAGER"]
 *   (report.write não incluído, employee permissions não são relevantes)
 */
