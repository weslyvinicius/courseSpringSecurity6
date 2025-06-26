package com.academy.springsecurity6full.service;

import com.academy.springsecurity6full.config.JwtConfig;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço responsável pela geração de tokens JWT.
 * <p>
 * Aplica os princípios SOLID:
 * - Single Responsibility: Responsável apenas pela geração de JWT
 * - Open/Closed: Extensível para diferentes tipos de token
 * - Dependency Inversion: Depende da abstração JwtEncoder
 * <p>
 * Segue padrões DDD:
 * - Encapsula a lógica de domínio relacionada a tokens
 * - Não possui dependências de infraestrutura direta
 */
@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtConfig jwtConfig;

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
     * Método genérico para gerar tokens com tempos customizados
     */
    private String generateToken(Authentication authentication, long expiration, ChronoUnit unit) throws Exception {
        Instant now = Instant.now();
        List<String> authorities = authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(authentication.getName())
                .claim("authorities", authorities)
                .claim("type", unit == ChronoUnit.HOURS ? "access" : "refresh") // Identifica tipo do token
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration, unit)))
                .signWith(jwtConfig.loadPrivateKey(), Jwts.SIG.RS256)
                .compact();
    }

    /**
     * Método genérico para gerar tokens para usuário específico
     */
    private String generateTokenForUser(String username, List<String> authorities,
                                        long expiration, ChronoUnit unit) throws Exception {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(username)
                .claim("authorities", authorities)
                .claim("type", unit == ChronoUnit.HOURS ? "access" : "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration, unit)))
                .signWith(jwtConfig.loadPrivateKey(), Jwts.SIG.RS256)
                .compact();
    }

    /**
     * Valida se o token é um refresh token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            String tokenType = claims.get("type", String.class);
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
            Claims claims = getClaimsFromToken(token);
            String username = claims.getSubject();
            return username.equals(userDetails.getUsername()) && !isTokenExpired(claims);
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

    // Métodos existentes permanecem inalterados...
    public Claims getClaimsFromToken(String token) throws Exception {
        return Jwts.parser()
                .verifyWith(jwtConfig.loadPublicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsernameFromToken(String token) throws Exception {
        return getClaimsFromToken(token).getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        try {
            Claims claims = getClaimsFromToken(token);
            String username = claims.getSubject();
            String tokenType = claims.get("type", String.class);

            // Access tokens devem ter type "access" ou null (compatibilidade)
            boolean isValidType = tokenType == null || "access".equals(tokenType);

            return username.equals(userDetails.getUsername())
                    && !isTokenExpired(claims)
                    && isValidType;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().toInstant().isBefore(Instant.now());
    }


}
