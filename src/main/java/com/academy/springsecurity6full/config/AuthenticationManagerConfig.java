package com.academy.springsecurity6full.config;

import com.academy.springsecurity6full.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuração do AuthenticationManager para JWT.
 *
 * O AuthenticationManager é necessário para validar credenciais
 * durante o processo de login (geração do token JWT).
 *
 * Componentes configurados:
 * - DaoAuthenticationProvider: Valida credenciais contra o banco de dados
 * - UserDetailsService: Carrega dados do usuário do banco
 * - PasswordEncoder: Compara senhas criptografadas
 *
 * Fluxo de autenticação:
 * 1. Cliente envia username/password para /auth/login
 * 2. AuthService chama AuthenticationManager.authenticate()
 * 3. DaoAuthenticationProvider carrega usuário via UserDetailsService
 * 4. PasswordEncoder valida a senha fornecida
 * 5. Se válido, retorna Authentication com authorities do usuário
 * 6. JwtService gera token baseado no Authentication
 */
@Configuration
@RequiredArgsConstructor
public class AuthenticationManagerConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Configura o AuthenticationManager com DaoAuthenticationProvider.
     *
     * O DaoAuthenticationProvider é responsável por:
     * - Carregar usuário do banco de dados via UserDetailsService
     * - Validar senha usando PasswordEncoder
     * - Retornar objeto Authentication com authorities do usuário
     *
     * @return AuthenticationManager configurado para validação de credenciais
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        // Configura o provider DAO para autenticação baseada em banco de dados
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

        // Define o serviço que carrega detalhes do usuário
        authProvider.setUserDetailsService(userDetailsService);

        // Define o encoder para validação de senhas
        authProvider.setPasswordEncoder(passwordEncoder);

        // Retorna o AuthenticationManager com o provider configurado
        return new ProviderManager(authProvider);
    }
}
