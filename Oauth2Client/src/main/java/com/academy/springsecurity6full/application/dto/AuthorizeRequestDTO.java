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
        String tokenEndpoint
) {

    public AuthorizeRequestDTO cloneAuthorizeRequestDTO(String state) {
        return new AuthorizeRequestDTO( this.clientId,
                this.clientSecret, this.redirectUri, this.responseType,
                this.grantType, this.scope, state,
                this.authorizationEndpoint, this.tokenEndpoint);
    }

}
