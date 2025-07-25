package com.academy.springsecurity6full.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.io.InputStream;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Configuração JWT usando chaves RSA para assinatura e verificação de tokens.
 *
 * Esta classe configura:
 * - JwtEncoder: Para criar/assinar novos tokens JWT
 * - JwtDecoder: Para validar/decodificar tokens JWT recebidos
 * - JWKSource: Fonte das chaves públicas para validação
 *
 * Utiliza o padrão de chaves RSA assimétricas onde:
 * - Chave privada: assina os tokens (apenas o servidor conhece)
 * - Chave pública: valida os tokens (pode ser compartilhada)
 */
@Configuration
public class JwtConfig {

    @Value("classpath:private.pem")
    private Resource privateKeyResource;

    @Value("classpath:public.pem")
    private Resource publicKeyResource;


    /**
     * Configuração das chaves RSA para JWT.
     * As chaves devem estar no formato PEM e localizadas no classpath.
     * Exemplo de configuração no application.properties:
     *
     *
     * # Para o Resource Server (validação do token)
     * spring.security.oauth2.resourceserver.jwt.public-key-location=classpath:keys/public-key.pem
     *
     * # Se você quiser usar a chave privada no JwtService (opcional)
     * app.jwt.private-key-location=classpath:keys/private-key.pem
     */

    /**
     * Bean responsável por decodificar/validar tokens JWT recebidos.
     * Utiliza a chave pública RSA para verificar a assinatura do token.
     */
    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        RSAPublicKey publicKey = loadPublicKey();
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    /**
     * Bean responsável por codificar/assinar novos tokens JWT.
     * Utiliza tanto a chave pública quanto a privada RSA.
     */
    @Bean
    public JwtEncoder jwtEncoder() throws Exception {
        RSAPublicKey publicKey = loadPublicKey();
        RSAPrivateKey privateKey = loadPrivateKey();

        // Cria o JWK (JSON Web Key) com as chaves RSA
        JWK jwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .build();

        // Configura a fonte de chaves para o encoder
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    /**
     * Carrega a chave pública RSA do arquivo PEM.
     * Remove headers/footers do PEM e decodifica a chave.
     */
    private RSAPublicKey loadPublicKey() throws Exception {
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
    private RSAPrivateKey loadPrivateKey() throws Exception {
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
