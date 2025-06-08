package com.academy.springsecurity6full.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

// Configuração de segurança com usuários em memória
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

	// Configura a cadeia de filtros de segurança do Spring Security
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/api/**").authenticated() // Exige autenticação para todos os endpoints /api/**
						.anyRequest().permitAll() // Permite acesso a outros endpoints sem autenticação
				)
				.httpBasic(); // Usa autenticação HTTP Basic para facilitar os testes
		return http.build();
	}

	// Configura usuários em memória para testes
	@Bean
	public UserDetailsService userDetailsService() {
		// Cria três usuários com diferentes papéis e permissões para testar os endpoints
		var admin = User.withUsername("admin")
				.password("{noop}password") // {noop} indica senha sem codificação (apenas para testes)
				.authorities("ROLE_ADMIN","READ_RESOURCE", "WRITE_RESOURCE") // Atribui permissões granulares
				.build();
		var manager = User.withUsername("manager")
				.password("{noop}password")
				.roles("MANAGER") // Atribui o papel ROLE_MANAGER
				.build();
		var user = User.withUsername("john")
				.password("{noop}password")
				.authorities("ROLE_USER","READ_RESOURCE", "WRITE_RESOURCE") // Atribui permissões granulares
				.build();
		return new InMemoryUserDetailsManager(admin, manager, user);
	}
}
