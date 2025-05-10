package com.academy.springsecurity6full.config;

import com.academy.springsecurity6full.repository.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

	private final UserDetailsServiceImpl userDetailsService;

	@Bean
	public SecurityFilterChain mySecurityFilterChain( HttpSecurity http ) throws Exception {

		//* As Roles dos users dever ser salvas no bando de dados no formado "ROLE_" ex: ROLE_ADMIN
		http.authorizeHttpRequests(configure ->
				configure
						.requestMatchers( "/h2-console/**" ).permitAll()
						.requestMatchers( "/h2-console" ).permitAll()
						.requestMatchers( "/logout" ).permitAll()
						.requestMatchers( HttpMethod.GET, "/api/employees").hasRole( "EMPLOYEE" )
						.requestMatchers( HttpMethod.GET, "/api/employees/**").hasRole( "EMPLOYEE" )
						.requestMatchers( HttpMethod.POST, "/api/employees").hasRole( "MANAGER" )
						.requestMatchers(HttpMethod.PUT, "/api/employees").hasRole("MANAGER")
						.requestMatchers( HttpMethod.DELETE, "/api/employees/**").hasRole( "ADMIN" )
						.anyRequest().authenticated()

		);

		// use http basic authentication
		http.httpBasic();

		// disable csrf
		http.csrf().disable();

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
