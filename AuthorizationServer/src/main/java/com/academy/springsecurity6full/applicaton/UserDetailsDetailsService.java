package com.academy.springsecurity6full.applicaton;


import com.academy.springsecurity6full.infrastructure.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Serviço para carregar detalhes do usuário a partir do repositório.
 * Implementa UserDetailsService para integração com o Spring Security.
 *
 * @Service - Indica que esta classe é um serviço do Spring
 * @RequiredArgsConstructor - Gera um construtor com os campos finais obrigatórios
 * @Transactional(readOnly = true) - Indica que os métodos deste serviço são somente leitura
 */

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserDetailsDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
