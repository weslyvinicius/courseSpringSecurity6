package com.academy.springsecurity6full.managers;

import com.academy.springsecurity6full.providers.CustomAuthenticationProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class CustomAuthenticationManager implements AuthenticationManager {

	private final String key;

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		var provider = new CustomAuthenticationProvider(key);
		if (provider.supports(authentication.getClass())) {
			return provider.authenticate(authentication);
		}

		return authentication;
	}
}
