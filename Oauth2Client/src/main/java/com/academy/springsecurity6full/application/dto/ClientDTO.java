package com.academy.springsecurity6full.application.dto;

import com.academy.springsecurity6full.domain.client.Client;

import java.util.List;

public record ClientDTO(
        String clientId,
        String clientSecret,
        String authorizationEndpoint,
        String tokenEndpoint,
        String redirectUri,
        List<String> scopes,
        String responseType,
        List<String> grantType,
        String name
) {
    public static ClientDTO fromDomain(Client client) {
        return new ClientDTO(
                client.clientId(),
                client.clientSecret(),
                client.authorizationEndpoint(),
                client.tokenEndpoint(),
                client.redirectUri(),
                client.scopes(),
                client.responseType(),
                client.grantType(),
                client.name()
        );
    }

    public Client toDomain() {
        return new Client(
                clientId, clientSecret, authorizationEndpoint, tokenEndpoint,
                redirectUri, scopes, responseType, grantType, name
        );
    }
}
