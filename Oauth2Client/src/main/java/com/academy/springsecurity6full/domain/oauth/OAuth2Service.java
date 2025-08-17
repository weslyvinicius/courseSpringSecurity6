package com.academy.springsecurity6full.domain.oauth;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final RestTemplate restTemplate;

    public TokenResponse exchangeCodeForToken(String tokenEndpoint, String clientId, String clientSecret,
                                              String code, String redirectUri, String grantType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", grantType);
        body.add("code", code);
        body.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<OAuth2TokenResponse> response = restTemplate.exchange(
                    tokenEndpoint, HttpMethod.POST, request, OAuth2TokenResponse.class);

            return convertToTokenResponse(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to exchange code for token: " + e.getMessage(), e);
        }
    }

    public TokenResponse refreshToken(String tokenEndpoint, String clientId, String clientSecret,
                                      String refreshToken, String grantType, String scope) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", grantType);
        body.add("refresh_token", refreshToken);

        if (scope != null && !scope.isEmpty()) {
            body.add("scope", scope);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<OAuth2TokenResponse> response = restTemplate.exchange(
                    tokenEndpoint, HttpMethod.POST, request, OAuth2TokenResponse.class);

            return convertToTokenResponse(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to refresh token: " + e.getMessage(), e);
        }
    }

    /**
     * Novo método para trocar authorization code por token usando PKCE
     * Não usa client_secret, apenas client_id e code_verifier
     */
    public TokenResponse exchangeCodeForTokenWithPKCE(String tokenEndpoint, String clientId,
                                                      String code, String redirectUri, String grantType,
                                                      String codeVerifier) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", grantType);
        body.add("client_id", clientId);
        body.add("code", code);
        body.add("redirect_uri", redirectUri);
        body.add("code_verifier", codeVerifier);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<OAuth2TokenResponse> response = restTemplate.exchange(
                    tokenEndpoint, HttpMethod.POST, request, OAuth2TokenResponse.class);

            return convertToTokenResponse(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to exchange code for token with PKCE: " + e.getMessage(), e);
        }
    }

    public TokenResponse clientCredentials(String tokenEndpoint, String clientId, String clientSecret,
                                           String grantType, String scope) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setBasicAuth(clientId, clientSecret);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", grantType);

        if (scope != null && !scope.isEmpty()) {
            body.add("scope", scope);
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<OAuth2TokenResponse> response = restTemplate.exchange(
                    tokenEndpoint, HttpMethod.POST, request, OAuth2TokenResponse.class);

            return convertToTokenResponse(response.getBody());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get client credentials token: " + e.getMessage(), e);
        }
    }

    private TokenResponse convertToTokenResponse(OAuth2TokenResponse oauth2Response) {
        return TokenResponse.of(
                oauth2Response.getAccessToken(),
                oauth2Response.getRefreshToken(),
                oauth2Response.getTokenType(),
                oauth2Response.getExpiresIn(),
                oauth2Response.getScope(),
                oauth2Response.getIdToken()
        );
    }
}
