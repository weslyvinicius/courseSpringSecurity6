package com.academy.springsecurity6full.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.UUID;

@Configuration
public class RegisteredClientConfig {

    /**
     * REPOSITÓRIO DE CLIENTES REGISTRADOS
     * <p>
     * Define quais aplicações (clientes) podem usar este Authorization Server.
     * Cada cliente precisa ser registrado com suas configurações específicas.
     */
    @Bean
    public RegisteredClientRepository registeredClientRepository() {

        // Registra um cliente OAuth2
        RegisteredClient r1 = RegisteredClient.withId(UUID.randomUUID().toString()) // ID único interno

                // CREDENCIAIS DO CLIENTE
                .clientId("my-client")                    // ID público do cliente
                .clientSecret("secret")                // Senha do cliente (deve ser criptografada em produção!)

                // ESCOPOS PERMITIDOS
                // Escopos definem que tipo de acesso o cliente pode solicitar
                .scope(OidcScopes.OPENID)             // Escopo obrigatório para OpenID Connect
                .scope(OidcScopes.PROFILE)            // Permite acessar informações do perfil do usuário

                // Após a autenticação, o usuário será redirecionado para esta URL
                // DEVE ser exatamente igual ao registrado (questão de segurança)
                .redirectUri("http://localhost:8081/oauth2/callback")  // URI corrigida
                .postLogoutRedirectUri("http://127.0.0.1:8080/")

                // MÉTODO DE AUTENTICAÇÃO DO CLIENTE
                // Como o cliente vai se autenticar no Authorization Server
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC) // Basic Auth

                // TIPOS DE GRANT PERMITIDOS
                // Authorization Code: fluxo mais seguro, recomendado para aplicações web
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                // Refresh Token: permite renovar tokens sem nova autenticação
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)

                .build();

        // Retorna um repositório em memória com o cliente registrado
        // Em produção, use JdbcRegisteredClientRepository para persistir no banco
        return new InMemoryRegisteredClientRepository(r1);
    }
}