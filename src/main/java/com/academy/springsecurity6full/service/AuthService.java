package com.academy.springsecurity6full.service;


import com.academy.springsecurity6full.repository.AuthorityEnum;
import com.academy.springsecurity6full.repository.UserEntity;
import com.academy.springsecurity6full.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Serviço de autenticação responsável por login e registro de usuários.
 * <p>
 * Implementa princípios SOLID:
 * - Single Responsibility: Gerencia apenas operações de autenticação
 * - Dependency Inversion: Depende de abstrações (interfaces)
 * - Open/Closed: Extensível para novos tipos de autenticação
 * <p>
 * Segue padrões DDD:
 * - Contém lógica de domínio para autenticação
 * - Coordena operações entre diferentes agregados (User, Authority)
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    /**
     * Autentica um usuário e retorna um token JWT.
     * <p>
     * Processo:
     * 1. Valida credenciais usando AuthenticationManager
     * 2. Extrai informações do usuário autenticado
     * 3. Gera token JWT com as authorities do usuário
     *
     * @param username Nome do usuário
     * @param password Senha do usuário
     * @return Token JWT para acesso autenticado
     */
    // ============================================
    // ABORDAGEM 1: AUTHENTICATIONMANAGER (AUTOMÁTICA)
    // ============================================
    public String authenticate(String username, String password) {
//        try {
            // O Spring Security faz TUDO automaticamente:
            // 1. Carrega usuário via UserDetailsService
            // 2. Valida senha com PasswordEncoder
            // 3. Verifica se conta não está bloqueada/expirada
            // 4. Popula authorities
            // 5. Cria Authentication completo
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            return jwtService.generateToken(auth);

//        } catch (BadCredentialsException e) {
//            // Exceções padronizadas do Spring Security
//            throw new RuntimeException("Credenciais inválidas");
//        } catch (DisabledException e) {
//            throw new RuntimeException("Conta desabilitada");
//        } catch (AccountExpiredException e) {
//            throw new RuntimeException("Conta expirada");
//        }
        // Spring Security tem muitas outras exceções específicas

        // Configuração necessária:
//        @Configuration
//        public class AuthConfig {
//            @Bean
//            public AuthenticationManager authenticationManager(
//                    UserDetailsService userDetailsService,
//                    PasswordEncoder passwordEncoder) {
//
//                DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//                provider.setUserDetailsService(userDetailsService);
//                provider.setPasswordEncoder(passwordEncoder);
//
//                return new ProviderManager(provider);
//            }
//        }
    }

    // ============================================
    // ABORDAGEM 2: MANUAL
    // ============================================
    public String authenticateManual(String username, String password) {
        try {
            // Você faz TUDO manualmente:
            UserDetails user = userDetailsService.loadUserByUsername(username);

            // 1. Validar senha
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new RuntimeException("Senha inválida");
            }

            // 2. Verificar se conta está ativa (você precisa implementar)
            if (!user.isEnabled()) {
                throw new RuntimeException("Conta desabilitada");
            }

            // 3. Verificar se conta não está bloqueada (você precisa implementar)
            if (!user.isAccountNonLocked()) {
                throw new RuntimeException("Conta bloqueada");
            }

            // 4. Verificar se conta não está expirada (você precisa implementar)
            if (!user.isAccountNonExpired()) {
                throw new RuntimeException("Conta expirada");
            }

            // 5. Verificar se credenciais não estão expiradas (você precisa implementar)
            if (!user.isCredentialsNonExpired()) {
                throw new RuntimeException("Credenciais expiradas");
            }

            // 6. Converter authorities manualmente
            List<String> authorities = user.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            return jwtService.generateTokenForUser(user.getUsername(), authorities);

        } catch (UsernameNotFoundException e) {
            throw new RuntimeException("Usuário não encontrado");
        }
    }

    // Não precisa configurar AuthenticationManager

// ============================================
// RESUMO DAS DIFERENÇAS
// ============================================

/*
AUTHENTICATIONMANAGER:
- Usa a infraestrutura completa do Spring Security
- Tratamento automático de todos os cenários (conta bloqueada, expirada, etc.)
- Exceções padronizadas e específicas
- Auditoria e logs automáticos
- Consistente com o resto da aplicação Spring Security
- Funciona com múltiplos providers (LDAP, Database, etc.)

MANUAL:
- Você implementa toda a lógica
- Precisa tratar manualmente todos os cenários
- Exceções genéricas
- Menos código de configuração
- Mais controle, mas mais trabalho

RECOMENDAÇÃO:
Use AuthenticationManager para aproveitar toda a infraestrutura
do Spring Security que você já está usando no resto da aplicação.
*/


    /**
     * Registra um novo usuário no sistema.
     * <p>
     * Processo:
     * 1. Verifica se o usuário já existe
     * 2. Cria novo usuário com senha criptografada
     * 3. Atribui authorities padrão (ROLE_EMPLOYEE)
     * 4. Salva usuário no banco de dados
     * 5. Retorna token JWT para acesso imediato
     *
     * @param username Nome do usuário
     * @param password Senha do usuário
     * @return Token JWT para o usuário recém-criado
     * @throws IllegalArgumentException se usuário já existe
     */
    @Transactional
    public Optional<String> register(String username, String password) {
        // Verifica se o usuário já existe
        if (userRepository.findByName(username).isPresent()) {
            return Optional.empty();
        }

        // Cria nova entidade de usuário
        UserEntity newUser = new UserEntity();
        newUser.setName(username);

        // Criptografa a senha antes de armazenar
        newUser.setPassword(passwordEncoder.encode(password));

        // Atribui authorities padrão para novos usuários
        // Por padrão, novos usuários recebem role EMPLOYEE com permissões básicas
        newUser.createAuthorities(
                AuthorityEnum.ROLE_EMPLOYEE,
                AuthorityEnum.READ_EMPLOYEE,
                AuthorityEnum.READ_REPORT
        );

        // Salva o usuário no banco de dados
        UserEntity savedUser = userRepository.save(newUser);

        // Converte authorities para string (formato esperado pelo JWT)
        List<String> authorities = savedUser.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        // Gera token JWT para acesso imediato após registro
        return Optional.ofNullable(jwtService.generateTokenForUser(savedUser.getUsername(), authorities));
    }
}
