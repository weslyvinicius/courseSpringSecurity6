package com.academy.springsecurity6full.config;

import com.academy.springsecurity6full.repository.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Optional;

/**
 * Configuração de segurança da aplicação.
 *
 * NOTA SOBRE AUTHORITIES VS ROLES:
 * No Spring Security, há um comportamento importante a ser observado:
 * - Uma role é automaticamente convertida para uma authority com prefixo "ROLE_"
 * - Quando usamos .roles("ADMIN"), o Spring internamente cria uma authority "ROLE_ADMIN"
 * - Quando definimos simultaneamente roles e authorities para o mesmo usuário,
 *   isso pode causar comportamentos inesperados porque o Spring gerencia ambos
 *   como authorities internamente.
 *
 * Por isso, esta implementação usa apenas authorities, incluindo as que representam roles
 * (prefixadas com "ROLE_"), tornando o código mais previsível e evitando conflitos.
 *
 * ORDEM DE PRECEDÊNCIA DAS REGRAS DE AUTORIZAÇÃO:
 * As regras definidas em authorizeHttpRequests() são avaliadas na ordem em que são
 * especificadas. A primeira regra que corresponde à requisição determina se o acesso
 * será concedido ou negado. Para garantir o comportamento correto:
 * - Regras mais específicas (ex.: "/api/reports/combined-auth") devem ser colocadas
 *   antes de regras mais genéricas (ex.: "/api/reports/**").
 * - Regras genéricas colocadas antes de regras específicas podem interceptar requisições
 *   prematuramente, levando a autorizações ou negações incorretas.
 * - Sempre revise a ordem das regras para evitar conflitos e garantir que a lógica de
 *   autorização seja aplicada como esperado.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final UserDetailsServiceImpl userDetailsService;

	@Bean
	public SecurityFilterChain mySecurityFilterChain(HttpSecurity http) throws Exception {

		//* As Roles dos users dever ser salvas no bando de dados no formado "ROLE_" ex: ROLE_ADMIN
		http.authorizeHttpRequests(configure ->
				configure
						// Configurações globais
						.requestMatchers("/logout").permitAll()
						.requestMatchers( "/h2-console/**" ).permitAll()
						.requestMatchers( "/h2-console" ).permitAll()

						// Endpoints de Employees
						.requestMatchers(HttpMethod.GET, "/api/employees").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/employees/**").hasAuthority("READ_EMPLOYEE")
						.requestMatchers(HttpMethod.POST, "/api/employees").hasAuthority("CREATE_EMPLOYEE")
						.requestMatchers(HttpMethod.PUT, "/api/employees/**").hasAuthority("UPDATE_EMPLOYEE")
						.requestMatchers(HttpMethod.DELETE, "/api/employees/**").hasAuthority("DELETE_EMPLOYEE")

						// Endpoints de Reports
						.requestMatchers(HttpMethod.GET, "/api/reports/combined-auth").access(
								(authentication, object) -> {
									Optional<Authentication> auth = Optional.ofNullable(authentication.get());
									return auth
											.map(a -> a.getAuthorities().stream()
													.anyMatch(authority -> authority.getAuthority().equals("ROLE_MANAGER")) &&
													a.getAuthorities().stream()
															.anyMatch(authority -> authority.getAuthority().equals("READ_REPORT")))
											.map(AuthorizationDecision::new)
											.orElse(new AuthorizationDecision(false));
								}
						)
						.requestMatchers(HttpMethod.GET, "/api/reports").hasAuthority("READ_REPORT")
						.requestMatchers(HttpMethod.GET, "/api/reports/**").hasAuthority("READ_REPORT")
						.requestMatchers(HttpMethod.POST, "/api/reports").hasAuthority("CREATE_REPORT")
						.requestMatchers(HttpMethod.PUT, "/api/reports/**").hasAuthority("UPDATE_REPORT")
						.requestMatchers(HttpMethod.DELETE, "/api/reports/**").hasAuthority("DELETE_REPORT")

						// Endpoints de Admin
						.requestMatchers(HttpMethod.GET, "/api/admin").permitAll()
						.requestMatchers("/api/admin/**").hasRole("ADMIN")

						// Qualquer outra requisição precisa de autenticação
						.anyRequest().authenticated()
		);

		// Use http basic authentication
		http.httpBasic();

		// Disable csrf
		http.csrf().disable();

		http.cors().disable();

		// Enable form login
		http.formLogin(Customizer.withDefaults());

		http.userDetailsService( userDetailsService );

		return http.build();
	}

	// Usando pass code text
	@Bean
	PasswordEncoder passwordEncoder(){
		return NoOpPasswordEncoder.getInstance();
	}


//  Usando ByCripyt
//	@Bean
//	PasswordEncoder passwordEncoder(){
//		return new BCryptPasswordEncoder();
//	}


}
