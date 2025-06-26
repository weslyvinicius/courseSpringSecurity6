package com.academy.springsecurity6full.config;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Utility class for JWT generation and validation.
 *
 * - Loads RSA public and private keys from resources.
 * - Generates JWTs with user details and authorities using modern JJWT API.
 * - Validates JWTs and extracts claims.
 */
@Component
public class JwtConfig {

    @Value("classpath:private.pem")
    private Resource privateKeyResource;

    @Value("classpath:public.pem")
    private Resource publicKeyResource;

    /**
     * Carrega a chave pública RSA do arquivo PEM.
     * Remove headers/footers do PEM e decodifica a chave.
     */
    public RSAPublicKey loadPublicKey() throws Exception {
        try (InputStream inputStream = publicKeyResource.getInputStream()) {
            String keyContent = new String(inputStream.readAllBytes())
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPublicKey) keyFactory.generatePublic(spec);
        }
    }

    /**
     * Carrega a chave privada RSA do arquivo PEM.
     * Remove headers/footers do PEM e decodifica a chave.
     */
    public RSAPrivateKey loadPrivateKey() throws Exception {
        try (InputStream inputStream = privateKeyResource.getInputStream()) {
            String keyContent = new String(inputStream.readAllBytes())
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s", "");

            byte[] keyBytes = Base64.getDecoder().decode(keyContent);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) keyFactory.generatePrivate(spec);
        }
    }
}
