package com.academy.springsecurity6full.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança da aplicação usando JWT.
 *
 * Principais mudanças da configuração básica para JWT:
 * 1. Substituição do Basic Auth por OAuth2 Resource Server com JWT
 * 2. Configuração de endpoints públicos para autenticação
 * 3. Política de sessão STATELESS (sem estado)
 * 4. Conversão de authorities do JWT para o formato do Spring Security
 *
 * NOTA SOBRE AUTHORITIES:
 * Mantém o padrão de usar apenas authorities (incluindo roles com prefixo ROLE_)
 * para evitar conflitos entre roles e authorities no Spring Security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

	/**
	 * Configura a cadeia de filtros de segurança para JWT.
	 *
	 * Principais configurações:
	 * - Endpoints públicos: /auth/login e /auth/register
	 * - Demais endpoints: requerem autenticação JWT
	 * - Sessão STATELESS: não mantém estado entre requisições
	 * - OAuth2 Resource Server: valida tokens JWT
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.authorizeHttpRequests(authorize -> authorize
				// Endpoints públicos - não requerem autenticação
				.requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
				.requestMatchers(HttpMethod.POST, "/auth/refresh").permitAll()
				.requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
				// Todos os outros endpoints requerem autenticação
				.anyRequest().authenticated()
		);

		// Configura OAuth2 Resource Server para validação de JWT
		http.oauth2ResourceServer(oauth2 -> oauth2
				.jwt(jwt -> jwt
						// Configura o conversor de authorities do JWT
						.jwtAuthenticationConverter(jwtAuthenticationConverter())
				)
		);

		// Política de sessão STATELESS - não mantém estado no servidor
		// Cada requisição deve conter o token JWT para autenticação
		http.sessionManagement(session ->
				session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
		);

		// Desabilita CSRF pois não utilizamos sessões (stateless)
		http.csrf(csrf -> csrf.disable());

		// Desabilita CORS (pode ser configurado conforme necessidade)
		http.cors(cors -> cors.disable());

		return http.build();
	}

	/**
	 * Configura o encoder de senha usando BCrypt.
	 * BCrypt é mais seguro que NoOpPasswordEncoder para produção.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * Configura o conversor de autenticação JWT.
	 *
	 * Este conversor é responsável por:
	 * 1. Extrair as authorities do token JWT
	 * 2. Converter para o formato esperado pelo Spring Security
	 * 3. Mapear corretamente roles e permissions
	 */
	@Bean
	public JwtAuthenticationConverter jwtAuthenticationConverter() {
		// Conversor de authorities do JWT
		JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();

		// Define o prefixo das authorities (vazio para usar nosso padrão)
		authoritiesConverter.setAuthorityPrefix("");

		// Define o nome da claim que contém as authorities no JWT
		authoritiesConverter.setAuthoritiesClaimName("authorities");

		// Configura o conversor principal
		JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
		jwtConverter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

		return jwtConverter;
	}
}