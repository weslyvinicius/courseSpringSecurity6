package com.academy.springsecurity6full.domain.oauth;

import java.time.LocalDateTime;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        String scope,
        LocalDateTime createdAt,
        String idToken
) {

    public static TokenResponse of (String accessToken, String refreshToken, String tokenType, Long expiresIn, String scope, String idToken) {
        return new TokenResponse(
                accessToken,
                refreshToken,
                tokenType,
                expiresIn,
                scope,
                LocalDateTime.now(),
                idToken
        );
    }
}
