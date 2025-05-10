package com.academy.springsecurity6full.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;

	@Override public UserDetails loadUserByUsername( String username ) throws UsernameNotFoundException {
		return userRepository.findByName( username )
				             .orElseThrow(() -> new UsernameNotFoundException( "User Not Found with username:"+username ));
	}
}
