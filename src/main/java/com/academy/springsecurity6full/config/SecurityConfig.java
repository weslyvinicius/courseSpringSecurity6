package com.academy.springsecurity6full.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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
public class SecurityConfig {

	@Bean
	public SecurityFilterChain mySecurityFilterChain(HttpSecurity http) throws Exception {

		http.authorizeHttpRequests(configure ->
				configure
						// Configurações globais
						.requestMatchers("/logout").permitAll()

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

		return http.build();
	}

	/**
	 * Criando usuários com authorities específicas
	 * Nota: As roles são definidas como authorities com prefixo "ROLE_"
	 */
	@Bean
	public InMemoryUserDetailsManager userDetailsManager() {
		User.UserBuilder users = User.withDefaultPasswordEncoder();

		// User com role EMPLOYEE e permissões de leitura
		UserDetails john = users
				.username("john")
				.password("j123456")
				.authorities("ROLE_EMPLOYEE", "READ_EMPLOYEE")
				.build();

		// User com role MANAGER e permissões de leitura, criação e atualização
		UserDetails mary = users
				.username("mary")
				.password("m123456")
				.authorities("ROLE_MANAGER", "READ_EMPLOYEE", "CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "READ_REPORT")
				.build();

		// User com role ADMIN e todas as permissões
		UserDetails susan = users
				.username("susan")
				.password("s123456")
				.authorities("ROLE_ADMIN", "READ_EMPLOYEE", "CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "DELETE_EMPLOYEE",
						"READ_REPORT", "CREATE_REPORT", "UPDATE_REPORT", "DELETE_REPORT")
				.build();

		return new InMemoryUserDetailsManager(john, mary, susan);
	}


	//Criando usuario em com encode de passaword text
	/**
	 @Bean
	 public InMemoryUserDetailsManager userDetailsManager(){
	 UserDetails john = User.builder()
	 .username( "john" )
	 .password( "{noop}j123456" )
	 .authorities( "ROLE_EMPLOYEE" )
	 .build();

	 UserDetails mary = User.builder()
	 .username( "mary" )
	 .password( "{noop}m123456" )
	 .authorities( "ROLE_MANAGER", "ROLE_EMPLOYEE" )
	 .build();

	 UserDetails susan = User.builder()
	 .username( "susan" )
	 .password( "{noop}s123456" )
	 .authorities( "ROLE_MANAGER", "ROLE_EMPLOYEE", "ROLE_ADMIN" )
	 .build();
	 return new InMemoryUserDetailsManager(john, mary, susan);
	 }
	 */

	//Criando usuario em com encode de passaword bcrypt

	/**
	 *
	 @Bean
	 public InMemoryUserDetailsManager userDetailsManager(){
	 UserDetails john = User.builder()
	 .username( "john" )
	 .password( "{bcrypt}$2a$12$gAZCDk7nJiApgwMjQSqAtuWdU1glJCRWjy4RjTcAdTnp2GEpw5UEC" )
	 .authorities( "ROLE_EMPLOYEE" )
	 .build();

	 UserDetails mary = User.builder()
	 .username( "mary" )
	 .password( "{bcrypt}$2a$12$P95pmS5cMCZSL/GfVq4chuSE1Qa.tnPew9atuDXS.QTtKO2iug36u" )
	 .authorities( "ROLE_MANAGER", "ROLE_EMPLOYEE" )
	 .build();

	 UserDetails susan = User.builder()
	 .username( "susan" )
	 .password( "{bcrypt}$2a$12$Hur7lxjp4nIuB30h/By22uKlrDmPP9SFPK0KAdhQ15MRMhJaUKDBK" )
	 .authorities( "ROLE_MANAGER", "ROLE_EMPLOYEE", "ROLE_ADMIN" )
	 .build();

	 return new InMemoryUserDetailsManager(john, mary, susan);
	 }
	 */


}
