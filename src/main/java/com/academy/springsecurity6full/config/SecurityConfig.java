package com.academy.springsecurity6full.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// Disable CSRF as JWT is stateless
				.csrf(csrf -> csrf.disable())
				// Disable CORS (configure as needed)
				.cors(cors -> cors.disable())
				// Set session management to stateless
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				// Configure endpoint permissions
				.authorizeHttpRequests(auth -> auth
						// Public endpoints
						.requestMatchers("/api/auth/login", "/api/auth/register",
								"/api/auth/refresh", "/api/auth/validate").permitAll()
						// All other endpoints require authentication
						.anyRequest().authenticated()
				)
				// Add JWT filter before UsernamePasswordAuthenticationFilter
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		// Use BCrypt for secure password hashing
		return new BCryptPasswordEncoder();
	}

	@Bean
	public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
		// Provide AuthenticationManager for login processing
		return authenticationConfiguration.getAuthenticationManager();
	}

}
