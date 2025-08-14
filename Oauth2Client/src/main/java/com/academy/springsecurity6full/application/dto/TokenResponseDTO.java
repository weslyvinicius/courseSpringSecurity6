package com.academy.springsecurity6full.application.dto;

import com.academy.springsecurity6full.domain.oauth.TokenResponse;

import java.time.LocalDateTime;

public record TokenResponseDTO(
        String accessToken,
        String refreshToken,
        String tokenType,
        Long expiresIn,
        String scope,
        LocalDateTime createdAt,
        String idToken
) {
    public static TokenResponseDTO fromDomain(TokenResponse tokenResponse) {
        return new TokenResponseDTO(
                tokenResponse.accessToken(),
                tokenResponse.refreshToken(),
                tokenResponse.tokenType(),
                tokenResponse.expiresIn(),
                tokenResponse.scope(),
                tokenResponse.createdAt(),
                tokenResponse.idToken()
        );
    }

}
