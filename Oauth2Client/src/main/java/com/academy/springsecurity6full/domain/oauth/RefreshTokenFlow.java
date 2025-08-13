package com.academy.springsecurity6full.domain.oauth;

public record RefreshTokenFlow(
        String clientId,
        String clientSecret,
        String refreshToken,
        String grantType,
        String scope,
        String idToken
) {
}
