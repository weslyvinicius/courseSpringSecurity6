package com.academy.springsecurity6full.application.dto;

public record TokenRequestDTO(
        String clientId,
        String clientSecret,
        String redirectUri,
        String grantType,
        String tokenEndpoint,
        String scope,
        String state,
        String code,
        // Campo para PKCE
        String codeVerifier
) {

    /**
     * Verifica se a requisição está usando PKCE
     * @return true se codeVerifier estiver presente
     */
    public boolean isPKCE() {
        return codeVerifier != null && !codeVerifier.isEmpty();
    }

    /**
     * Verifica se é um fluxo tradicional (com client_secret)
     * @return true se client_secret estiver presente e não for PKCE
     */
    public boolean isTraditionalFlow() {
        return !isPKCE() && clientSecret != null && !clientSecret.isEmpty();
    }
}