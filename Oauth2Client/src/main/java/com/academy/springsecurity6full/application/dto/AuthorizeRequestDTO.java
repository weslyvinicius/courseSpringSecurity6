package com.academy.springsecurity6full.application.dto;


public record AuthorizeRequestDTO(
        String clientId,
        String clientSecret,
        String redirectUri,
        String responseType,
        String grantType,
        String scope,
        String state,
        String authorizationEndpoint,
        String tokenEndpoint,
        // Novos campos para PKCE
        String codeChallenge,
        String codeChallengeMethod,
        String codeVerifier
) {

    public AuthorizeRequestDTO cloneAuthorizeRequestDTO(String state) {
        return new AuthorizeRequestDTO(
                this.clientId,
                this.clientSecret,
                this.redirectUri,
                this.responseType,
                this.grantType,
                this.scope,
                state,
                this.authorizationEndpoint,
                this.tokenEndpoint,
                this.codeChallenge,
                this.codeChallengeMethod,
                this.codeVerifier
        );
    }

    /**
     * Verifica se a requisição está usando PKCE
     * @return true se codeChallenge estiver presente
     */
    public boolean isPKCE() {
        return codeChallenge != null && !codeChallenge.isEmpty();
    }

    /**
     * Verifica se é um fluxo tradicional (com client_secret)
     * @return true se client_secret estiver presente e não for PKCE
     */
    public boolean isTraditionalFlow() {
        return !isPKCE() && clientSecret != null && !clientSecret.isEmpty();
    }

}
