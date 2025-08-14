package com.academy.springsecurity6full.application.dto;

public record ClientCredentialsRequestDTO(
        String tokenEndpoint,
        String clientId,
        String clientSecret,
        String grantType,
        String scope
) {
    public ClientCredentialsRequestDTO {
        if (grantType == null || grantType.isEmpty()) {
            grantType = "client_credentials";
        }
    }
}
