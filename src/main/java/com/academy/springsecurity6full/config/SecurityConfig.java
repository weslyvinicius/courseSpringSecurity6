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

		http.csrf().disable();

		http.authorizeHttpRequests( config ->
						config.requestMatchers( "/logout" ).permitAll()
						      .requestMatchers( "/h2-console/**" ).permitAll()
						      .requestMatchers( "/h2-console" ).permitAll()

							  //* AS Roles dever ser salvas no bando de dados no formado "ROLE_" ex: ROLE_ADMIN
							  .requestMatchers( HttpMethod.GET,    "/api/employees"   ).hasRole("EMPLOYEE")
							  .requestMatchers( HttpMethod.GET,    "/api/employees/**").hasRole("EMPLOYEE")
							  .requestMatchers( HttpMethod.POST,   "/api/employees"   ).hasRole("MANAGER")
							  .requestMatchers( HttpMethod.PUT,    "/api/employees"   ).hasRole("MANAGER")
							  .requestMatchers( HttpMethod.DELETE, "/api/employees/**").hasRole("ADMIN")
						.anyRequest().authenticated());

		http.userDetailsService( userDetailsService );

		http.formLogin(Customizer.withDefaults() );
		http.httpBasic();

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
