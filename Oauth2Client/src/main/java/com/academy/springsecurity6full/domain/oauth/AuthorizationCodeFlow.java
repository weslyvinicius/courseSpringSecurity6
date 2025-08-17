package com.academy.springsecurity6full.domain.oauth;

public record AuthorizationCodeFlow(
        String clientId,
        String redirectUri,
        String responseType,
        String scope,
        String state,
        String codeChallenge,
        String codeChallengeMethod
) {

    public String buildAuthorizationUrl(String authorizationEndpoint) {
        StringBuilder url = new StringBuilder(authorizationEndpoint);
        url.append("?response_type=").append(responseType);
        url.append("&client_id=").append(clientId);
        url.append("&redirect_uri=").append(redirectUri);

        if (scope != null && !scope.isEmpty()) {
            url.append("&scope=").append(scope);
        }

        if (state != null && !state.isEmpty()) {
            url.append("&state=").append(state);
        }

        // Adicionar parâmetros PKCE se presentes
        if (codeChallenge != null && !codeChallenge.isEmpty()) {
            url.append("&code_challenge=").append(codeChallenge);

            // Se codeChallengeMethod não for especificado, usar "S256" como padrão
            String method = (codeChallengeMethod != null && !codeChallengeMethod.isEmpty())
                    ? codeChallengeMethod
                    : "S256";
            url.append("&code_challenge_method=").append(method);
        }

        return url.toString();
    }

    /**
     * Verifica se este fluxo está usando PKCE
     * @return true se codeChallenge estiver presente
     */
    public boolean isPKCE() {
        return codeChallenge != null && !codeChallenge.isEmpty();
    }
}