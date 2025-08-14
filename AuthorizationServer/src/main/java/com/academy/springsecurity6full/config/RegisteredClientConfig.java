package com.academy.springsecurity6full.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
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
        RegisteredClient r1 = createAuthorizationCodeClient();

        // Registra um cliente para Client Credentials Grant
        RegisteredClient r2 = createClientCredentialsClient();

        return new InMemoryRegisteredClientRepository(r1,r2);
    }

    private RegisteredClient createAuthorizationCodeClient() {
        return RegisteredClient.withId(UUID.randomUUID().toString()) // ID único interno

                // CREDENCIAIS DO CLIENTE
                .clientId("my-client")                    // ID público do cliente
                .clientSecret("secret")                // Senha do cliente (deve ser criptografada em produção!)

                // ESCOPOS PERMITIDOS
                // Escopos definem que tipo de acesso o cliente pode solicitar
                .scope(OidcScopes.OPENID)             // Escopo obrigatório para OpenID Connect
                .scope(OidcScopes.PROFILE)            // Permite acessar informações do perfil do usuário

                // Após a autenticação, o usuário será redirecionado para esta URL
                // DEVE ser exatamente igual ao registrado (questão de segurança)
                .redirectUri("http://localhost:8081/oauth2/callback")
                .postLogoutRedirectUri("http://127.0.0.1:8080/")

                // MÉTODO DE AUTENTICAÇÃO DO CLIENTE
                // Como o cliente vai se autenticar no Authorization Server
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC) // Basic Auth

                // TIPOS DE GRANT PERMITIDOS
                // Authorization Code: fluxo mais seguro, recomendado para aplicações web
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                // Refresh Token: permite renovar tokens sem nova autenticação
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)

                // CONFIGURAÇÕES DE TOKEN
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))    // Token expira em 1 hora
                        .refreshTokenTimeToLive(Duration.ofHours(2))   // Token de refresh expira em 2 horas
                        .build())

                .build();
    }

    private RegisteredClient createClientCredentialsClient() {
        return // Cliente para Client Credentials Grant
                 RegisteredClient.withId(UUID.randomUUID().toString())

                // CREDENCIAIS DO CLIENTE
                .clientId("api-client")                    // ID público do cliente
                .clientSecret("api-secret")                // Senha do cliente

                // ESCOPOS PERMITIDOS para este cliente
                // Em Client Credentials, os escopos definem que recursos/operações
                // o cliente pode acessar
                .scope("read")                            // Permissão de leitura
                .scope("write")                           // Permissão de escrita
                .scope("admin")                           // Permissões administrativas

                // MÉTODO DE AUTENTICAÇÃO DO CLIENTE
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)  // Basic Auth
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)   // Form POST

                // GRANT TYPE - Client Credentials
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)

                // CONFIGURAÇÕES ESPECÍFICAS DO CLIENTE
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(false)  // Não precisa de consentimento do usuário
                        .build())

                // CONFIGURAÇÕES DE TOKEN
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))    // Token expira em 1 hora
                        .build())

                .build();


    }
}