package com.academy.springsecurity6full.providers;

import com.academy.springsecurity6full.authentication.CustomAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Provider responsável pela lógica de autenticação
 * Esta classe:
 * 1. Recebe uma chave de autenticação através do header da requisição
 * 2. Compara com a chave secreta configurada na aplicação
 * 3. Retorna um objeto de autenticação com status autenticado se as chaves coincidirem
 * 4. Lança uma exceção caso as chaves não coincidam
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

	private final UserDetailsService userDetailsService;
	private final PasswordEncoder passwordEncoder;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		String username;
		String password;

		// Suporta UsernamePasswordAuthenticationToken (usado pelo Basic Auth)
		if (authentication instanceof UsernamePasswordAuthenticationToken token) {
			username = token.getName();
			password = token.getCredentials() != null ? token.getCredentials().toString() : null;
		}
		// Suporta CustomAuthentication
		else if (authentication instanceof CustomAuthentication ca) {
			username = ca.getUsername();
			password = ca.getPassword();
		} else {
			throw new BadCredentialsException("Unsupported authentication type.");
		}

		// Carrega o usuário do UserDetailsService
		UserDetails userDetails = userDetailsService.loadUserByUsername(username);

		// Verifica a senha
		if (password != null && passwordEncoder.matches(password, userDetails.getPassword())) {
			return new CustomAuthentication(true, username, null); // Senha nula para não expor
		}

		throw new BadCredentialsException("Invalid username or password.");
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return UsernamePasswordAuthenticationToken.class.equals(authentication) ||
				CustomAuthentication.class.equals(authentication);
	}
}
