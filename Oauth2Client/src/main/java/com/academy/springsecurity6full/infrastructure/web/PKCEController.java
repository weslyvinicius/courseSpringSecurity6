package com.academy.springsecurity6full.infrastructure.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api")
public class PKCEController {

    @GetMapping("/pkce/generate")
    public ResponseEntity<Map<String, String>> generatePKCE() {
        try {
            // Gerar code_verifier (43-128 caracteres, base64url)
            String codeVerifier = generateCodeVerifier();

            // Gerar code_challenge usando SHA256
            String codeChallenge = generateCodeChallenge(codeVerifier);

            log.info("Generated PKCE values - verifier length: {}, challenge: {}",
                    codeVerifier.length(), codeChallenge.substring(0, 10) + "...");

            return ResponseEntity.ok(Map.of(
                    "codeVerifier", codeVerifier,
                    "codeChallenge", codeChallenge,
                    "codeChallengeMethod", "S256"
            ));

        } catch (Exception e) {
            log.error("Error generating PKCE values", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    private String generateCodeVerifier() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32]; // 43 caracteres em base64url
        random.nextBytes(bytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }

    private String generateCodeChallenge(String verifier) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(verifier.getBytes("UTF-8"));

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(hash);
    }
}
