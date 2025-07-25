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
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.util.UUID;

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

	/**
	 * CODIFICADOR DE SENHAS
	 *
	 * ATENÇÃO: NoOpPasswordEncoder é APENAS para desenvolvimento!
	 * Ele NÃO criptografa as senhas, deixando-as em texto plano.
	 *
	 * Em produção, use:
	 * - BCryptPasswordEncoder (recomendado)
	 * - Argon2PasswordEncoder
	 * - SCryptPasswordEncoder
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		// NUNCA use em produção! Senhas ficam em texto plano!
		return NoOpPasswordEncoder.getInstance();

		// Para produção, use algo como:
		// return new BCryptPasswordEncoder();
	}

	/**
	 * REPOSITÓRIO DE CLIENTES REGISTRADOS
	 *
	 * Define quais aplicações (clientes) podem usar este Authorization Server.
	 * Cada cliente precisa ser registrado com suas configurações específicas.
	 */
	@Bean
	public RegisteredClientRepository registeredClientRepository() {

		// Registra um cliente OAuth2
		RegisteredClient r1 = RegisteredClient.withId(UUID.randomUUID().toString()) // ID único interno

				// CREDENCIAIS DO CLIENTE
				.clientId("my-client")                    // ID público do cliente
				.clientSecret("secret")                // Senha do cliente (deve ser criptografada em produção!)

				// ESCOPOS PERMITIDOS
				// Escopos definem que tipo de acesso o cliente pode solicitar
				.scope(OidcScopes.OPENID)             // Escopo obrigatório para OpenID Connect
				.scope(OidcScopes.PROFILE)            // Permite acessar informações do perfil do usuário

				// URI DE REDIRECIONAMENTO
				// Após a autenticação, o usuário será redirecionado para esta URL
				// DEVE ser exatamente igual ao registrado (questão de segurança)
				.redirectUri("http://localhost:8081/callback")
				.postLogoutRedirectUri("http://127.0.0.1:8080/")

				// MÉTODO DE AUTENTICAÇÃO DO CLIENTE
				// Como o cliente vai se autenticar no Authorization Server
				.clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC) // Basic Auth

				// TIPOS DE GRANT PERMITIDOS
				// Authorization Code: fluxo mais seguro, recomendado para aplicações web
				.authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
				// Refresh Token: permite renovar tokens sem nova autenticação
				.authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)

				.build();

		// Retorna um repositório em memória com o cliente registrado
		// Em produção, use JdbcRegisteredClientRepository para persistir no banco
		return new InMemoryRegisteredClientRepository(r1);
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