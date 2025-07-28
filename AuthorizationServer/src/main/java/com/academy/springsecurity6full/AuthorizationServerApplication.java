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

            // CLIENTE 1 - Aplicação Web Completa (todos os escopos)
            RegisteredClientEntity webClient = new RegisteredClientEntity();
            webClient.setId(UUID.randomUUID().toString());
            webClient.setClientId("web-app");
            webClient.setClientSecret(passwordEncoder.encode("web-secret"));
            webClient.setClientName("Web Application - Full Access");
            webClient.setClientAuthenticationMethods(Set.of("client_secret_basic"));
            webClient.setAuthorizationGrantTypes(Set.of("authorization_code", "refresh_token"));
            webClient.setRedirectUris(Set.of("http://localhost:8081/callback"));
            webClient.setPostLogoutRedirectUris(Set.of("http://localhost:8081/logout"));

            // ESCOPOS COMPLETOS - App web pode solicitar tudo
            webClient.setScopes(Set.of(
                    "openid",           // OpenID Connect obrigatório
                    "profile",          // Dados do perfil do usuário
                    "email",            // Email do usuário
                    "employee.read",    // Leitura de funcionários
                    "employee.write",   // Escrita de funcionários (criar/atualizar)
                    "employee.delete",  // Exclusão de funcionários
                    "report.read",      // Leitura de relatórios
                    "report.write",     // Escrita de relatórios (criar/atualizar)
                    "report.delete",    // Exclusão de relatórios
                    "admin"             // Acesso administrativo completo
            ));

            // CLIENTE 2 - Mobile App (escopos limitados - apenas leitura)
            RegisteredClientEntity mobileClient = new RegisteredClientEntity();
            mobileClient.setId(UUID.randomUUID().toString());
            mobileClient.setClientId("mobile-app");
            mobileClient.setClientSecret(passwordEncoder.encode("mobile-secret"));
            mobileClient.setClientName("Mobile Application - Read Only");
            mobileClient.setClientAuthenticationMethods(Set.of("client_secret_post"));
            mobileClient.setAuthorizationGrantTypes(Set.of("authorization_code", "refresh_token"));
            mobileClient.setRedirectUris(Set.of("com.academy.app://callback"));
            mobileClient.setPostLogoutRedirectUris(Set.of("com.academy.app://logout"));

            // ESCOPOS LIMITADOS - App mobile só pode ler
            mobileClient.setScopes(Set.of(
                    "openid",           // OpenID Connect obrigatório
                    "profile",          // Dados do perfil do usuário
                    "employee.read",    // Apenas leitura de funcionários
                    "report.read"       // Apenas leitura de relatórios
                    // Sem escopos de escrita ou delete!
            ));

            // CLIENTE 3 - Dashboard Gerencial (escopos intermediários)
            RegisteredClientEntity dashboardClient = new RegisteredClientEntity();
            dashboardClient.setId(UUID.randomUUID().toString());
            dashboardClient.setClientId("dashboard-app");
            dashboardClient.setClientSecret(passwordEncoder.encode("dashboard-secret"));
            dashboardClient.setClientName("Management Dashboard");
            dashboardClient.setClientAuthenticationMethods(Set.of("client_secret_basic"));
            dashboardClient.setAuthorizationGrantTypes(Set.of("authorization_code", "refresh_token"));
            dashboardClient.setRedirectUris(Set.of("http://localhost:8082/callback"));
            dashboardClient.setPostLogoutRedirectUris(Set.of("http://localhost:8082/logout"));

            // ESCOPOS GERENCIAIS - Sem delete, sem admin completo
            dashboardClient.setScopes(Set.of(
                    "openid",
                    "profile",
                    "employee.read",    // Pode ler funcionários
                    "employee.write",   // Pode criar/atualizar funcionários
                    "report.read",      // Pode ler relatórios
                    "report.write"      // Pode criar/atualizar relatórios
                    // Sem employee.delete, report.delete ou admin
            ));

            // CLIENTE 4 - API Service (Client Credentials - sem usuário)
            RegisteredClientEntity apiClient = new RegisteredClientEntity();
            apiClient.setId(UUID.randomUUID().toString());
            apiClient.setClientId("api-service");
            apiClient.setClientSecret(passwordEncoder.encode("api-secret"));
            apiClient.setClientName("API Service - Machine to Machine");
            apiClient.setClientAuthenticationMethods(Set.of("client_secret_basic"));
            apiClient.setAuthorizationGrantTypes(Set.of("client_credentials"));
            apiClient.setRedirectUris(Set.of()); // Não precisa para client_credentials
            apiClient.setPostLogoutRedirectUris(Set.of());

            // ESCOPOS PARA APIS - Operações de sistema
            apiClient.setScopes(Set.of(
                    "api.read",         // Leitura via API
                    "api.write",        // Escrita via API
                    "system.integration" // Integrações de sistema
            ));

            // CLIENTE 5 - Relatórios App (apenas relatórios)
            RegisteredClientEntity reportsClient = new RegisteredClientEntity();
            reportsClient.setId(UUID.randomUUID().toString());
            reportsClient.setClientId("reports-app");
            reportsClient.setClientSecret(passwordEncoder.encode("reports-secret"));
            reportsClient.setClientName("Reports Application");
            reportsClient.setClientAuthenticationMethods(Set.of("client_secret_basic"));
            reportsClient.setAuthorizationGrantTypes(Set.of("authorization_code", "refresh_token"));
            reportsClient.setRedirectUris(Set.of("http://localhost:8083/callback"));
            reportsClient.setPostLogoutRedirectUris(Set.of("http://localhost:8083/logout"));

            // ESCOPOS ESPECÍFICOS PARA RELATÓRIOS
            reportsClient.setScopes(Set.of(
                    "openid",
                    "profile",
                    "report.read",      // Apenas relatórios
                    "report.write"
                    // Sem acesso a employees
            ));

            // Salvar todos os clientes
            jpaRegisteredClientRepository.save(webClient);
            jpaRegisteredClientRepository.save(mobileClient);
            jpaRegisteredClientRepository.save(dashboardClient);
            jpaRegisteredClientRepository.save(apiClient);
            jpaRegisteredClientRepository.save(reportsClient);

            log.info("===== CLIENTES REGISTRADOS =====");
            log.info("Web App - Escopos: {}", webClient.getScopes());
            log.info("Mobile App - Escopos: {}", mobileClient.getScopes());
            log.info("Dashboard - Escopos: {}", dashboardClient.getScopes());
            log.info("API Service - Escopos: {}", apiClient.getScopes());
            log.info("Reports App - Escopos: {}", reportsClient.getScopes());

            // ===== CENÁRIOS DE TESTE =====
            log.info("===== CENÁRIOS DE TESTE =====");
            log.info("1. John (Employee) + Mobile App = Apenas leitura de employees");
            log.info("2. Mary (Manager) + Dashboard = Leitura + escrita (sem delete)");
            log.info("3. Susan (Admin) + Web App = Acesso completo");
            log.info("4. Mary (Manager) + Mobile App = Apenas leitura (mesmo sendo manager)");
            log.info("5. Susan (Admin) + Reports App = Apenas relatórios (mesmo sendo admin)");
        };
    }
}