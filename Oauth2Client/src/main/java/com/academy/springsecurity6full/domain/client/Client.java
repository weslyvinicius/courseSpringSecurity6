package com.academy.springsecurity6full.domain.client;

import java.util.List;

public record Client(
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
}
