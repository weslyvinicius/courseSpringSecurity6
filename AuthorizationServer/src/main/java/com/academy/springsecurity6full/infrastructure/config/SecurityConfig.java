package com.academy.springsecurity6full.infrastructure.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;


/**
 * Classe de configuração de segurança para OAuth2 Authorization Server
 *
 * @Configuration - Indica que esta classe contém definições de beans do Spring
 * @EnableWebSecurity - Habilita a configuração de segurança web do Spring Security
 * @RequiredArgsConstructor - Gera construtor com argumentos obrigatórios (do Lombok)
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	/**
	 * PRIMEIRO FILTRO DE SEGURANÇA - AUTHORIZATION SERVER
	 *
	 * Este SecurityFilterChain tem @Order(1), ou seja, MAIOR PRIORIDADE.
	 * É responsável por configurar os endpoints específicos do OAuth2 Authorization Server.
	 *
	 * Endpoints que serão interceptados por este filtro:
	 * - /oauth2/authorize (endpoint de autorização)
	 * - /oauth2/token (endpoint para obter tokens)
	 * - /oauth2/jwks (endpoint para chaves públicas JWT)
	 * - /.well-known/oauth-authorization-server (endpoint de descoberta)
	 * - /.well-known/openid_configuration (endpoint OpenID Connect)
	 */

	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
			throws Exception {
		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = OAuth2AuthorizationServerConfigurer.authorizationServer();

		http
				.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
				.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
				.csrf(csrf -> csrf.ignoringRequestMatchers(
						authorizationServerConfigurer.getEndpointsMatcher()
				))
				.with(authorizationServerConfigurer, (authorizationServer) ->
						authorizationServer
								.oidc(Customizer.withDefaults())	// Enable OpenID Connect 1.0
				)
				// Redirect to the login page when not authenticated from the
				// authorization endpoint
				.exceptionHandling((exceptions) -> exceptions
						.defaultAuthenticationEntryPointFor(
								new LoginUrlAuthenticationEntryPoint("/login"),
								new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
						)
				);

		return http.build();
	}

	/**
	 * SEGUNDO FILTRO DE SEGURANÇA - DEFAULT
	 *
	 * Este SecurityFilterChain tem @Order(2), ou seja, MENOR PRIORIDADE.
	 * É responsável por configurar a segurança para o resto da aplicação.
	 *
	 * ATUALIZAÇÃO: Adicionadas rotas públicas para o simulador de cliente OAuth2
	 */
	@Bean
	@Order(2)
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
			throws Exception {
		     http
				.authorizeHttpRequests((authorize) ->
						authorize
								.anyRequest().authenticated()
				)
				// Form login handles the redirect to the login page from the
				// authorization server filter chain
				.formLogin(Customizer.withDefaults());


		return http.build();
	}

	/**
	 * CONFIGURAÇÕES DO AUTHORIZATION SERVER
	 *
	 * Define configurações específicas do servidor de autorização,
	 * como URLs dos endpoints, configurações de JWT, etc.
	 */
	@Bean
	public AuthorizationServerSettings authorizationServerSettings() {
		// Usando configurações padrão
		// Os endpoints padrão serão:
		// - /oauth2/authorize (autorização)
		// - /oauth2/token (obtenção de tokens)
		// - /oauth2/jwks (chaves públicas)
		// - /oauth2/revoke (revogação de tokens)
		// - /oauth2/introspect (introspecção de tokens)
		return AuthorizationServerSettings.builder()
				// Você pode customizar URLs dos endpoints aqui se necessário:
				// .authorizationEndpoint("/custom/authorize")
				// .tokenEndpoint("/custom/token")
				.build();
	}

	/*
	 * FLUXO COMPLETO DE AUTENTICAÇÃO:
	 *
	 * 1. Cliente redireciona usuário para: /oauth2/authorize?client_id=client&...
	 * 2. Usuário não está autenticado → redirecionado para /login
	 * 3. Usuário faz login com "user"/"password"
	 * 4. Sistema gera código de autorização e redireciona para: https://springone.io/authorized?code=...
	 * 5. Cliente troca código por token: POST /oauth2/token
	 * 6. Cliente usa token para acessar recursos protegidos
	 *
	 * ENDPOINTS IMPORTANTES:
	 * - GET /oauth2/authorize - Inicia processo de autorização
	 * - POST /oauth2/token - Obter/renovar tokens
	 * - GET /oauth2/jwks - Chaves públicas para validar JWT
	 * - GET /.well-known/oauth-authorization-server - Metadados do servidor
	 */
}