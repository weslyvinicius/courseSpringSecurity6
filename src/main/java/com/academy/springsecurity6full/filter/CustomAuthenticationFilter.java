package com.academy.springsecurity6full.filter;

import com.academy.springsecurity6full.authentication.CustomAuthentication;
import com.academy.springsecurity6full.managers.CustomAuthenticationManager;
import com.academy.springsecurity6full.providers.CustomAuthenticationProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;


@Component
@AllArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final String key;

	@Override
	protected void doFilterInternal( HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		CustomAuthenticationManager manager = new CustomAuthenticationManager(key);
		var requestKey = request.getHeader("x-api-key");

		if (requestKey == null || "null".equals(requestKey)) {
			filterChain.doFilter(request, response);
		}

		var auth = new CustomAuthentication(requestKey);

		try {
			var a = manager.authenticate(auth);
			if (a.isAuthenticated()) {
				SecurityContextHolder.getContext().setAuthentication(a);
				filterChain.doFilter(request, response);
			} else {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			}
		} catch (AuthenticationException | IOException | ServletException e) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		}
	}



}
