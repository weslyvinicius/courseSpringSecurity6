package com.academy.springsecurity6full.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
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


	/**
	 * Criando usuario em com encode de passaword default
	 */

	@Bean
	public InMemoryUserDetailsManager userDetailsManager(){
		User.UserBuilder users = User.withDefaultPasswordEncoder();
		UserDetails john =
				users.username( "john" )
				.password( "j123456" )
				.roles( "EMPLOYEE" )
				.build();

		UserDetails mary =
				users.username( "mary" )
				.password( "m123456" )
				.roles( "MANAGER","EMPLOYEE" )
				.build();

		UserDetails susan =
				 users.username( "susan" )
				.password( "s123456" )
				.roles( "MANAGER","EMPLOYEE","ADMIN" )
				.build();

		return new InMemoryUserDetailsManager(john, mary, susan);
	}


	//Criando usuario em com encode de passaword text

	/**
	 *
	@Bean
	public InMemoryUserDetailsManager userDetailsManager(){
		UserDetails john = User.builder()
							   .username( "john" )
	                           .password( "{noop}j123456" )
							   .roles( "EMPLOYEE" )
							   .build();

		UserDetails mary = User.builder()
							   .username( "mary" )
							   .password( "{noop}m123456" )
							   .roles( "MANAGER","EMPLOYEE" )
							   .build();

		UserDetails susan = User.builder()
								.username( "susan" )
								.password( "{noop}s123456" )
								.roles( "MANAGER","EMPLOYEE","ADMIN" )
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
									.roles( "EMPLOYEE" )
									.build();

			 UserDetails mary = User.builder()
									.username( "mary" )
									.password( "{bcrypt}$2a$12$P95pmS5cMCZSL/GfVq4chuSE1Qa.tnPew9atuDXS.QTtKO2iug36u" )
									.roles( "MANAGER","EMPLOYEE" )
									.build();

			 UserDetails susan = User.builder()
									 .username( "susan" )
									 .password( "{bcrypt}$2a$12$Hur7lxjp4nIuB30h/By22uKlrDmPP9SFPK0KAdhQ15MRMhJaUKDBK" )
									 .roles( "MANAGER","EMPLOYEE","ADMIN" )
									 .build();

    	 return new InMemoryUserDetailsManager(john, mary, susan);
	 }
	 */


}
