package com.academy.springsecurity6full;

import com.academy.springsecurity6full.domain.AuthorityEnum;
import com.academy.springsecurity6full.infrastructure.repository.CustomRegisteredClientRepository;
import com.academy.springsecurity6full.infrastructure.repository.JpaRegisteredClientRepository;
import com.academy.springsecurity6full.infrastructure.repository.UserRepository;
import com.academy.springsecurity6full.infrastructure.repository.entity.RegisteredClientEntity;
import com.academy.springsecurity6full.infrastructure.repository.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

@Slf4j
@SpringBootApplication
public class AuthorizationServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthorizationServerApplication.class, args);
    }

    @Bean
    CommandLineRunner commandLineRunner(UserRepository userRepository,
                                        CustomRegisteredClientRepository customRegisteredClientRepository,
                                        PasswordEncoder passwordEncoder) {
        return args -> {

            // ===== CRIAÇÃO DE USUÁRIOS COM AUTHORITIES GRANULARES =====

            // USUÁRIO 1: Employee - Acesso básico (apenas leitura)
            UserEntity john = new UserEntity();
            john.setName("john");
            john.setPassword(passwordEncoder.encode("j123456"));
            john.createAuthorities(
                    AuthorityEnum.ROLE_EMPLOYEE,    // Role básica
                    AuthorityEnum.READ_EMPLOYEE     // Pode ler dados de funcionários
            );

            // USUÁRIO 2: Manager - Acesso intermediário (leitura + escrita limitada)
            UserEntity mary = new UserEntity();
            mary.setName("mary");
            mary.setPassword(passwordEncoder.encode("m123456"));
            mary.createAuthorities(
                    AuthorityEnum.ROLE_MANAGER,     // Role de gerente
                    AuthorityEnum.READ_EMPLOYEE,    // Pode ler funcionários
                    AuthorityEnum.CREATE_EMPLOYEE,  // Pode criar funcionários
                    AuthorityEnum.UPDATE_EMPLOYEE,  // Pode atualizar funcionários
                    AuthorityEnum.READ_REPORT       // Pode ler relatórios
            );

            // USUÁRIO 3: Admin - Acesso completo (todas as authorities)
            UserEntity susan = new UserEntity();
            susan.setName("susan");
            susan.setPassword(passwordEncoder.encode("s123456"));
            susan.createAuthorities(
                    AuthorityEnum.ROLE_ADMIN,       // Role administrativa
                    AuthorityEnum.READ_EMPLOYEE,    // Leitura de funcionários
                    AuthorityEnum.CREATE_EMPLOYEE,  // Criação de funcionários
                    AuthorityEnum.UPDATE_EMPLOYEE,  // Atualização de funcionários
                    AuthorityEnum.DELETE_EMPLOYEE,  // Exclusão de funcionários
                    AuthorityEnum.READ_REPORT,      // Leitura de relatórios
                    AuthorityEnum.CREATE_REPORT,    // Criação de relatórios
                    AuthorityEnum.UPDATE_REPORT,    // Atualização de relatórios
                    AuthorityEnum.DELETE_REPORT     // Exclusão de relatórios
            );

            userRepository.save(john);
            userRepository.save(mary);
            userRepository.save(susan);

            log.info("===== USUÁRIOS CRIADOS =====");
            log.info("John (Employee): {}", john.getAuthorities());
            log.info("Mary (Manager): {}", mary.getAuthorities());
            log.info("Susan (Admin): {}", susan.getAuthorities());

            // ===== CRIAÇÃO DE REGISTERED CLIENTS COM ESCOPOS ESPECÍFICOS =====

            log.info("Iniciando inicialização dos clientes OAuth2...");

            // Verifica se os clientes já existem para evitar duplicação
            if (customRegisteredClientRepository.findByClientId("my-client") == null) {
                createAndSaveAuthorizationCodeClient(customRegisteredClientRepository, passwordEncoder);
                log.info("Cliente 'my-client' criado com sucesso!");
            } else {
                log.info("Cliente 'my-client' já existe no banco de dados.");
            }

            if (customRegisteredClientRepository.findByClientId("api-client") == null) {
                createAndSaveClientCredentialsClient(customRegisteredClientRepository, passwordEncoder);
                log.info("Cliente 'api-client' criado com sucesso!");
            } else {
                log.info("Cliente 'api-client' já existe no banco de dados.");
            }

            log.info("Inicialização dos clientes OAuth2 concluída!");


        };
    }

    private void createAndSaveAuthorizationCodeClient(
            CustomRegisteredClientRepository customRegisteredClientRepository,
            PasswordEncoder passwordEncoder
    ) {
        RegisteredClient authorizationCodeClient = RegisteredClient
                .withId(UUID.randomUUID().toString()) // ID único interno

                // CREDENCIAIS DO CLIENTE
                .clientId("my-client")                    // ID público do cliente
                .clientSecret(passwordEncoder.encode("secret"))  // Senha criptografada

                // NOME DO CLIENTE
                .clientName("My Web Application")         // Nome amigável do cliente

                // ESCOPOS PERMITIDOS
                // Escopos definem que tipo de acesso o cliente pode solicitar
                .scope(OidcScopes.OPENID)             // Escopo obrigatório para OpenID Connect
                .scope(OidcScopes.PROFILE)            // Permite acessar informações do perfil do usuário

                // REDIRECT URIs
                // Após a autenticação, o usuário será redirecionado para estas URLs
                // DEVE ser exatamente igual ao registrado (questão de segurança)
                .redirectUri("http://localhost:8081/oauth2/callback")
                .redirectUri("http://127.0.0.1:8081/oauth2/callback")

                // POST LOGOUT REDIRECT URIs
                .postLogoutRedirectUri("http://127.0.0.1:8080/")
                .postLogoutRedirectUri("http://localhost:8080/")

                // MÉTODO DE AUTENTICAÇÃO DO CLIENTE
                // Como o cliente vai se autenticar no Authorization Server
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC) // Basic Auth
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)  // Form POST

                // TIPOS DE GRANT PERMITIDOS
                // Authorization Code: fluxo mais seguro, recomendado para aplicações web
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                // Refresh Token: permite renovar tokens sem nova autenticação
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)

                // CONFIGURAÇÕES DO CLIENTE
                .clientSettings(ClientSettings.builder()
                        .requireAuthorizationConsent(true)   // Requer consentimento do usuário
                        .requireProofKey(false)               // PKCE não obrigatório (pode habilitar para mais segurança)
                        .build())

                // CONFIGURAÇÕES DE TOKEN
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(1))     // Token expira em 1 hora
                        .refreshTokenTimeToLive(Duration.ofHours(24))   // Refresh token expira em 24 horas
                        .reuseRefreshTokens(true)                       // Reutiliza refresh tokens
                        .build())

                .build();

        customRegisteredClientRepository.save(authorizationCodeClient);
    }

    private void createAndSaveClientCredentialsClient(
            CustomRegisteredClientRepository customRegisteredClientRepository,
            PasswordEncoder passwordEncoder
    ) {
        RegisteredClient clientCredentialsClient = RegisteredClient
                .withId(UUID.randomUUID().toString())

                // CREDENCIAIS DO CLIENTE
                .clientId("api-client")                           // ID público do cliente
                .clientSecret(passwordEncoder.encode("api-secret")) // Senha criptografada

                // NOME DO CLIENTE
                .clientName("API Client Application")            // Nome amigável do cliente

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
                        .requireProofKey(false)               // PKCE não necessário para client credentials
                        .build())

                // CONFIGURAÇÕES DE TOKEN
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofHours(2))    // Token expira em 2 horas
                        .build())

                .build();

        customRegisteredClientRepository.save(clientCredentialsClient);
    }
}
