package com.academy.springsecurity6full.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
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

    private final JwtDecoder jwtDecoder;

    private final long JWT_EXPIRATION_HOURS = 1; // Access token: 1 hora

    private final long REFRESH_EXPIRATION_DAYS = 7; // Refresh token: 7 dias

    /**
     * Gera access token com menor tempo de expiração
     */
    public String generateAccessToken(Authentication authentication) throws Exception {
        return generateToken(authentication, JWT_EXPIRATION_HOURS, ChronoUnit.HOURS);
    }

    /**
     * Gera refresh token com maior tempo de expiração
     */
    public String generateRefreshToken(Authentication authentication) throws Exception {
        return generateToken(authentication, REFRESH_EXPIRATION_DAYS, ChronoUnit.DAYS);
    }

    /**
     * Gera access token para usuário específico
     */
    public String generateAccessTokenForUser(String username, List<String> authorities) throws Exception {
        return generateTokenForUser(username, authorities, JWT_EXPIRATION_HOURS, ChronoUnit.HOURS);
    }

    /**
     * Gera refresh token para usuário específico
     */
    public String generateRefreshTokenForUser(String username, List<String> authorities) throws Exception {
        return generateTokenForUser(username, authorities, REFRESH_EXPIRATION_DAYS, ChronoUnit.DAYS);
    }

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
    private String generateToken(Authentication authentication, long expiration, ChronoUnit unit) throws Exception {
        Instant now = Instant.now();

        // Extrai as authorities como List<String>
        List<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("spring-security-jwt")
                .issuedAt(now)
                .claim("type", unit == ChronoUnit.HOURS ? "access" : "refresh") // Identifica tipo do token
                .issuedAt(Instant.now())
                .expiresAt(now.plus(expiration, ChronoUnit.HOURS))
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
    public String generateTokenForUser(String username, List<String> authorities,
                                       long expiration, ChronoUnit unit) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("spring-security-jwt")
                .issuedAt(now)
                .claim("type", unit == ChronoUnit.HOURS ? "access" : "refresh")
                .expiresAt(now.plus(expiration, ChronoUnit.HOURS))
                .subject(username)
                .claim("authorities", authorities) // Agora espera uma List
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    /**
     * Valida se o token é um refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String tokenType = jwt.getClaimAsString("type");
            return "refresh".equals(tokenType);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida refresh token
     */
    public boolean validateRefreshToken(String token, UserDetails userDetails) {
        try {
            if (!isRefreshToken(token)) {
                return false;
            }
            Jwt jwt = jwtDecoder.decode(token);
            String username = jwt.getSubject();
            return username.equals(userDetails.getUsername()) && !isTokenExpired(jwt);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Obtém tempo de expiração em segundos
     */
    public long getExpirationTime() {
        return JWT_EXPIRATION_HOURS * 3600; // Converte horas para segundos
    }

    public Jwt getJwtFromToken(String token) {
        return jwtDecoder.decode(token);
    }

    public String getUsernameFromToken(String token) {
        return getJwtFromToken(token).getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            Jwt jwt = getJwtFromToken(token);
            String username = jwt.getSubject();
            String tokenType = jwt.getClaimAsString("type");

            // Access tokens devem ter type "access" ou null (compatibilidade)
            boolean isValidType = tokenType == null || "access".equals(tokenType);

            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(jwt)
                    && isValidType;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(Jwt jwt) {
        return jwt.getExpiresAt().isBefore(Instant.now());
    }

}