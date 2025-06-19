package com.academy.springsecurity6full.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.stream.Collectors;

/**
 * Utility class for JWT generation and validation.
 *
 * - Loads RSA public and private keys from resources.
 * - Generates JWTs with user details and authorities using modern JJWT API.
 * - Validates JWTs and extracts claims.
 */
@Component
public class JwtUtil {

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private final long JWT_EXPIRATION_HOURS = 3; // 3 hours
    private JwtParser jwtParser;

    @PostConstruct
    public void init() throws Exception {
        // Load private key
        ClassPathResource privateKeyResource = new ClassPathResource("private.pem");
        try (InputStream is = privateKeyResource.getInputStream()) {
            String privateKeyPEM = new String(is.readAllBytes())
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            privateKey = kf.generatePrivate(spec);
        }

        // Load public key
        ClassPathResource publicKeyResource = new ClassPathResource("public.pem");
        try (InputStream is = publicKeyResource.getInputStream()) {
            String publicKeyPEM = new String(is.readAllBytes())
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");
            byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            publicKey = kf.generatePublic(spec);
        }

        // Initialize JwtParser with public key
        jwtParser = Jwts.parser()
                .verifyWith(publicKey)
                .build();
    }

    public String generateToken(UserDetails userDetails) {
        // Extract authorities
        String authorities = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // Generate JWT using modern API
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("authorities", authorities)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(JWT_EXPIRATION_HOURS, ChronoUnit.HOURS)))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    public Claims getClaimsFromToken(String token) {
        // Parse and validate JWT
        return jwtParser
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getUsernameFromToken(String token) {
        // Extract username from claims
        return getClaimsFromToken(token).getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        // Validate token integrity and expiration
        try {
            Claims claims = getClaimsFromToken(token);
            String username = claims.getSubject();
            return username.equals(userDetails.getUsername()) && !isTokenExpired(claims);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isTokenExpired(Claims claims) {
        // Check if token is expired
        return claims.getExpiration().toInstant().isBefore(Instant.now());
    }
}
