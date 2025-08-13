package com.academy.springsecurity6full.domain.oauth;

public record AuthorizationCodeFlow(
        String clientId,
        String redirectUri,
        String responseType,
        String scope,
        String state
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

        return url.toString();
    }

}
