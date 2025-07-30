package com.academy.springsecurity6full.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.time.Duration;
import java.util.UUID;

/**
 * Classe de configuração de segurança para OAuth2 Authorization Server
 * CONFIGURADO PARA CLIENT CREDENTIALS GRANT
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
	 * Para Client Credentials, os endpoints principais são:
	 * - /oauth2/token (endpoint para obter tokens - PRINCIPAL)
	 * - /oauth2/jwks (endpoint para chaves públicas JWT)
	 * - /oauth2/introspect (endpoint para introspecção de tokens)
	 * - /oauth2/revoke (endpoint para revogação de tokens)
	 */
	@Bean
	@Order(1)
	public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
			throws Exception {
		OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
				OAuth2AuthorizationServerConfigurer.authorizationServer();

		http
				.securityMatcher(authorizationServerConfigurer.getEndpointsMatcher())
				.authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
				.csrf(csrf -> csrf.ignoringRequestMatchers(
						authorizationServerConfigurer.getEndpointsMatcher()
				))
				.with(authorizationServerConfigurer, Customizer.withDefaults());

		return http.build();
	}

	/**
	 * SEGUNDO FILTRO DE SEGURANÇA - DEFAULT
	 *
	 * Para Client Credentials, este filtro é menos importante pois
	 * a maior parte das interações será via API diretamente no endpoint /oauth2/token
	 */
	@Bean
	@Order(2)
	public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
			throws Exception {
		http
				.authorizeHttpRequests((authorize) ->
						authorize
								.requestMatchers("/client-credentials").permitAll()
								.anyRequest().authenticated()
				)
				.formLogin(Customizer.withDefaults());

		return http.build();
	}

	/**
	 * SERVIÇO DE DETALHES DO USUÁRIO
	 *
	 * Para Client Credentials Grant, este bean não é estritamente necessário
	 * pois não há autenticação de usuário final envolvida.
	 * Mantemos apenas para compatibilidade caso você queira usar outros grants futuramente.
	 */
	@Bean
	public UserDetailsService userDetailsService() {
		var u1 = User.withUsername("user")
				.password("password")
				.authorities("read")
				.build();

		return new InMemoryUserDetailsManager(u1);
	}

	/**
	 * CODIFICADOR DE SENHAS
	 *
	 * ATENÇÃO: NoOpPasswordEncoder é APENAS para desenvolvimento!
	 * Em produção, use BCryptPasswordEncoder ou similar.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		// NUNCA use em produção! Para desenvolvimento apenas!
		return NoOpPasswordEncoder.getInstance();

		// Para produção:
		// return new BCryptPasswordEncoder();
	}

	/**
	 * REPOSITÓRIO DE CLIENTES REGISTRADOS - CLIENT CREDENTIALS
	 *
	 * Para Client Credentials Grant, definimos clientes que representam
	 * aplicações/serviços que vão acessar recursos em nome de si mesmas,
	 * não de um usuário específico.
	 */
	@Bean
	public RegisteredClientRepository registeredClientRepository() {

		// Cliente para Client Credentials Grant
		RegisteredClient clientCredentialsClient = RegisteredClient
				.withId(UUID.randomUUID().toString())

				// CREDENCIAIS DO CLIENTE
				.clientId("api-client")                    // ID público do cliente
				.clientSecret("api-secret")                // Senha do cliente (criptografar em produção!)

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
						.refreshTokenTimeToLive(Duration.ofHours(24))   // N/A para Client Credentials
						.reuseRefreshTokens(false)                     // N/A para Client Credentials
						.build())

				.build();

		// Cliente adicional para testes (opcional)
		RegisteredClient testClient = RegisteredClient
				.withId(UUID.randomUUID().toString())
				.clientId("test-client")
				.clientSecret("test-secret")
				.scope("test")
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
				.tokenSettings(TokenSettings.builder()
						.accessTokenTimeToLive(Duration.ofMinutes(30))  // Token de teste expira em 30 min
						.build())
				.build();

		return new InMemoryRegisteredClientRepository(clientCredentialsClient, testClient);
	}

	/**
	 * CONFIGURAÇÕES DO AUTHORIZATION SERVER
	 */
	@Bean
	public AuthorizationServerSettings authorizationServerSettings() {
		return AuthorizationServerSettings.builder()
				// URLs padrão dos endpoints:
				// POST /oauth2/token - PRINCIPAL para Client Credentials
				// GET /oauth2/jwks - Para validação de JWT
				// POST /oauth2/introspect - Para introspecção de tokens
				// POST /oauth2/revoke - Para revogar tokens
				.build();
	}

	/*
	 * FLUXO CLIENT CREDENTIALS GRANT:
	 *
	 * 1. Cliente faz POST para /oauth2/token com:
	 *    - grant_type=client_credentials
	 *    - scope=read write (opcional)
	 *    - Autenticação: Basic Auth ou client_secret no body
	 *
	 * 2. Authorization Server valida credenciais do cliente
	 *
	 * 3. Se válido, retorna access_token imediatamente
	 *    (sem authorization code, sem redirect, sem usuário)
	 *
	 * 4. Cliente usa access_token para acessar APIs protegidas
	 *
	 * EXEMPLO DE REQUISIÇÃO:
	 * POST /oauth2/token
	 * Authorization: Basic YXBpLWNsaWVudDphcGktc2VjcmV0 (api-client:api-secret em Base64)
	 * Content-Type: application/x-www-form-urlencoded
	 *
	 * grant_type=client_credentials&scope=read write
	 *
	 * RESPOSTA:
	 * {
	 *   "access_token": "eyJhbGciOiJSUzI1NiIs...",
	 *   "token_type": "Bearer",
	 *   "expires_in": 3600,
	 *   "scope": "read write"
	 * }
	 *
	 * ENDPOINTS IMPORTANTES:
	 * - POST /oauth2/token - Obter tokens (PRINCIPAL)
	 * - GET /oauth2/jwks - Chaves públicas para validar JWT
	 * - POST /oauth2/introspect - Verificar se token é válido
	 * - POST /oauth2/revoke - Revogar token
	 */
}