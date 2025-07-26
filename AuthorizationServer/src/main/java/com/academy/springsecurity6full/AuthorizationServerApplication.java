package com.academy.springsecurity6full;

import com.academy.springsecurity6full.repository.JpaRegisteredClientRepository;
import com.academy.springsecurity6full.repository.UserRepository;
import com.academy.springsecurity6full.repository.entity.AuthorityEnum;
import com.academy.springsecurity6full.repository.entity.RegisteredClientEntity;
import com.academy.springsecurity6full.repository.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.Set;
import java.util.UUID;

@Slf4j
@EnableJpaRepositories
@SpringBootApplication
public class AuthorizationServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthorizationServerApplication.class, args);
    }

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner commandLineRunner(UserRepository userRepository,
                                        JpaRegisteredClientRepository jpaRegisteredClientRepository,
                                        PasswordEncoder passwordEncoder) {
        return args -> {

            // ===== CRIAÇÃO DE USUÁRIOS =====
            UserEntity john = new UserEntity();
            john.setName("john");
            john.setPassword(passwordEncoder.encode("j123456"));
            john.createAuthorities(AuthorityEnum.ROLE_EMPLOYEE, AuthorityEnum.READ_EMPLOYEE);

            UserEntity mary = new UserEntity();
            mary.setName("mary");
            mary.setPassword(passwordEncoder.encode("m123456"));
            mary.createAuthorities(AuthorityEnum.ROLE_MANAGER,
                    AuthorityEnum.READ_EMPLOYEE,
                    AuthorityEnum.CREATE_EMPLOYEE,
                    AuthorityEnum.UPDATE_EMPLOYEE,
                    AuthorityEnum.READ_REPORT);

            UserEntity susan = new UserEntity();
            susan.setName("susan");
            susan.setPassword(passwordEncoder.encode("s123456"));
            susan.createAuthorities(AuthorityEnum.ROLE_ADMIN,
                    AuthorityEnum.READ_EMPLOYEE,
                    AuthorityEnum.CREATE_EMPLOYEE,
                    AuthorityEnum.UPDATE_EMPLOYEE,
                    AuthorityEnum.DELETE_EMPLOYEE,
                    AuthorityEnum.READ_REPORT,
                    AuthorityEnum.CREATE_REPORT,
                    AuthorityEnum.UPDATE_REPORT,
                    AuthorityEnum.DELETE_REPORT);

            userRepository.save(john);
            userRepository.save(mary);
            userRepository.save(susan);

            log.info("Users created: {}, {}, {}", john.getName(), mary.getName(), susan.getName());

            // ===== CRIAÇÃO DE REGISTERED CLIENTS =====

            // Cliente 1 - Aplicação Web Principal
            RegisteredClientEntity webClient = new RegisteredClientEntity();
            webClient.setId(UUID.randomUUID().toString());
            webClient.setClientId("my-client");
            webClient.setClientSecret(passwordEncoder.encode("secret"));
            webClient.setClientName("Web Application Client");
            webClient.setClientAuthenticationMethods(Set.of("client_secret_basic"));
            webClient.setAuthorizationGrantTypes(Set.of("authorization_code", "refresh_token"));
            webClient.setRedirectUris(Set.of("http://localhost:8081/callback"));
            webClient.setPostLogoutRedirectUris(Set.of("http://127.0.0.1:8080/"));
            webClient.setScopes(Set.of("openid", "profile"));

            // Cliente 2 - Mobile App
            RegisteredClientEntity mobileClient = new RegisteredClientEntity();
            mobileClient.setId(UUID.randomUUID().toString());
            mobileClient.setClientId("mobile-client");
            mobileClient.setClientSecret(passwordEncoder.encode("mobile-secret"));
            mobileClient.setClientName("Mobile Application");
            mobileClient.setClientAuthenticationMethods(Set.of("client_secret_post"));
            mobileClient.setAuthorizationGrantTypes(Set.of("authorization_code", "refresh_token"));
            mobileClient.setRedirectUris(Set.of("com.academy.app://callback"));
            mobileClient.setPostLogoutRedirectUris(Set.of("com.academy.app://logout"));
            mobileClient.setScopes(Set.of("openid", "profile", "email"));

            // Cliente 3 - API Service (Client Credentials)
            RegisteredClientEntity apiClient = new RegisteredClientEntity();
            apiClient.setId(UUID.randomUUID().toString());
            apiClient.setClientId("api-service");
            apiClient.setClientSecret(passwordEncoder.encode("api-secret"));
            apiClient.setClientName("API Service Client");
            apiClient.setClientAuthenticationMethods(Set.of("client_secret_basic"));
            apiClient.setAuthorizationGrantTypes(Set.of("client_credentials"));
            apiClient.setRedirectUris(Set.of()); // Não precisa para client_credentials
            apiClient.setPostLogoutRedirectUris(Set.of());
            apiClient.setScopes(Set.of("read", "write"));

            jpaRegisteredClientRepository.save(webClient);
            jpaRegisteredClientRepository.save(mobileClient);
            jpaRegisteredClientRepository.save(apiClient);

            log.info("Registered Clients created: {}, {}, {}",
                    webClient.getClientName(),
                    mobileClient.getClientName(),
                    apiClient.getClientName());
        };
    }

}
