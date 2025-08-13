package com.academy.springsecurity6full.config.token;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.UUID;

/**
 * Configuração JWT com geração dinâmica de chaves RSA.
 * Gera um novo par de chaves a cada inicialização da aplicação.
 *
 * Adequado para ambientes de desenvolvimento/testes.
 * Para produção, considere usar chaves persistentes.
 */
//@Configuration
public class JwtConfigDynamic {

    /**
     * Gera um par de chaves RSA 2048-bit dinamicamente
     */
    private KeyPair generateRsaKey() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            return keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao gerar chaves RSA", ex);
        }
    }

    /**
     * Configura a fonte de chaves JWK (JSON Web Key)
     */
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        // Cria a chave RSA com um ID único
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    /**
     * Bean para decodificar/validar tokens JWT
     */
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        try {
            RSAPublicKey publicKey = (RSAPublicKey) ((ImmutableJWKSet<SecurityContext>) jwkSource)
                    .getJWKSet()
                    .getKeys()
                    .get(0)
                    .toRSAKey()
                    .toPublicKey();

            return NimbusJwtDecoder.withPublicKey(publicKey).build();
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao configurar JwtDecoder", ex);
        }
    }

    /**
     * Bean para codificar/assinar tokens JWT
     */
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }
}
