package com.academy.springsecurity6full.managers;

import com.academy.springsecurity6full.providers.CustomAuthenticationProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * Gerenciador de autenticação customizado
 * Esta classe é responsável por:
 * 1. Receber uma solicitação de autenticação
 * 2. Verificar se existe um provider adequado para processar a autenticação
 * 3. Delegar a autenticação para o provider correspondente
 * 4. Lançar exceção caso a autenticação falhe
 */
@Component
@AllArgsConstructor
public class CustomAuthenticationManager implements AuthenticationManager {

	private final CustomAuthenticationProvider provider;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		if (provider.supports(authentication.getClass())) {
			return provider.authenticate(authentication);
		}
		throw new BadCredentialsException("Unsupported authentication type.");
	}
}
