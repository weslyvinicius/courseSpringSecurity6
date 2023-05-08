package com.academy.springsecurity6full.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain mySecurityFilterChain( HttpSecurity http ) throws Exception {

		http.csrf()
				.disable()
				.authorizeHttpRequests()
				.requestMatchers( HttpMethod.GET,"/myfree" ).permitAll()
				.requestMatchers( "/logout" ).permitAll()
				.anyRequest().authenticated()
				.and()
				.formLogin( Customizer.withDefaults())
				.httpBasic()
				.and()
				.cors();

		// http.addFilterBefore( new myFilter, UsernamePasswordAuthenticationFilter.class  ) // informo ao sprint security um filter
		// a ser executado antes.

		return http.build();
	}





}
