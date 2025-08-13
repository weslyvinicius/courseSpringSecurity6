package com.academy.springsecurity6full.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class UserDetailsConfig {

    /**
     * SERVIÇO DE DETALHES DO USUÁRIO
     *
     * Define como o Spring Security vai buscar informações dos usuários.
     * Neste caso, está usando um usuário em memória (apenas para desenvolvimento/testes).
     *
     * Em produção, normalmente você implementaria um UserDetailsService customizado
     * que busca usuários de um banco de dados.
     */
    @Bean
    public UserDetailsService userDetailsService() {

        // Cria um usuário em memória para testes
        var u1 = User.withUsername("user")           // Nome de usuário: "user"
                .password("password")                 // Senha: "password" (sem criptografia!)
                .authorities("read")                  // Permissão: "read"
                .build();

        // Retorna um gerenciador de usuários em memória
        // Em produção, você usaria JdbcUserDetailsManager ou uma implementação customizada
        return new InMemoryUserDetailsManager(u1);
    }
}
