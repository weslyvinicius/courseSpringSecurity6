package com.academy.springsecurity6full.filter;

import com.academy.springsecurity6full.authentication.CustomAuthentication;
import com.academy.springsecurity6full.managers.CustomAuthenticationManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro customizado que intercepta todas as requisições HTTP
 * Este filtro é responsável por:
 * 1. Extrair a chave do header da requisição
 * 2. Criar um objeto de autenticação não autenticado
 * 3. Delegar a autenticação para o manager
 * 4. Estabelecer o contexto de segurança se a autenticação for bem-sucedida
 */
@Component
@AllArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

	private final CustomAuthenticationManager customAuthenticationManager;

	@Override
	protected void doFilterInternal( HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {

		// 1. create an authentication object which is not yet authenticated
		// 2. delegate the authentication object to the manager
		// 3. get back the authentication from the manager
		// 4. if the object is authenticated then send request to the next filter in the chain
		String key = String.valueOf(request.getHeader("key"));
		CustomAuthentication ca = new CustomAuthentication(false, key);

		var a = customAuthenticationManager.authenticate(ca);

		if (a.isAuthenticated()) {
			SecurityContextHolder.getContext().setAuthentication(a);
			filterChain.doFilter(request, response); // only when authentication worked
		}
	}
}
