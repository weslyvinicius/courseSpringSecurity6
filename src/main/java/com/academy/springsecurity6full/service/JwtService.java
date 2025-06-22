package com.academy.springsecurity6full.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pela geração de tokens JWT.
 *
 * Aplica os princípios SOLID:
 * - Single Responsibility: Responsável apenas pela geração de JWT
 * - Open/Closed: Extensível para diferentes tipos de token
 * - Dependency Inversion: Depende da abstração JwtEncoder
 *
 * Segue padrões DDD:
 * - Encapsula a lógica de domínio relacionada a tokens
 * - Não possui dependências de infraestrutura direta
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;

    // Tempo de expiração do token em horas
    private static final long JWT_EXPIRATION_HOURS = 24;

    /**
     * Gera um token JWT baseado na autenticação do usuário.
     *
     * O token contém:
     * - sub: username do usuário (subject)
     * - iat: timestamp de criação (issued at)
     * - exp: timestamp de expiração
     * - authorities: lista de permissões do usuário
     *
     * @param authentication Objeto de autenticação contendo dados do usuário
     * @return Token JWT como string
     */
    public String generateToken(Authentication authentication) {
        Instant now = Instant.now();

        // Extrai as authorities como List<String>
        List<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("spring-security-jwt")
                .issuedAt(now)
                .expiresAt(now.plus(JWT_EXPIRATION_HOURS, ChronoUnit.HOURS))
                .subject(authentication.getName())
                .claim("authorities", authorities) // Agora envia como List
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * Gera um token JWT para um usuário específico com authorities customizadas.
     * Útil para casos onde o token é gerado sem autenticação prévia (ex: registro).
     *
     * @param username Nome do usuário
     * @param authorities Permissões do usuário
     * @return Token JWT como string
     */
    public String generateTokenForUser(String username, List<String> authorities) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("spring-security-jwt")
                .issuedAt(now)
                .expiresAt(now.plus(JWT_EXPIRATION_HOURS, ChronoUnit.HOURS))
                .subject(username)
                .claim("authorities", authorities) // Agora espera uma List
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}