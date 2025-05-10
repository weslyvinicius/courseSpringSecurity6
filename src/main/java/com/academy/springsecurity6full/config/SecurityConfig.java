package com.academy.springsecurity6full.config;

import com.academy.springsecurity6full.repository.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

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
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

	private final UserDetailsServiceImpl userDetailsService;

	@Bean
	public SecurityFilterChain mySecurityFilterChain( HttpSecurity http ) throws Exception {

		http.authorizeHttpRequests(configure ->
				configure
						// allow do acess to lougout default
						.requestMatchers( "/logout" ).permitAll()
						.anyRequest().authenticated() // all other requests need to be authenticated
		);

		// use http basic authentication
		http.httpBasic();

		// disable csrf
		http.csrf().disable();

		http.cors().disable();

		//Enable form to login
		http.formLogin( Customizer.withDefaults());

		http.userDetailsService( userDetailsService );

		// http.addFilterBefore( new myFilter, UsernamePasswordAuthenticationFilter.class  ) // informo ao sprint security um filter
		// a ser executado antes.

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
