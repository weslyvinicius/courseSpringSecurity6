package com.academy.springsecurity6full.application.dto;

public record RefreshTokenRequestDTO(
        String clientId,
        String clientSecret,
        String refreshToken,
        String grantType,
        String scope,
        String tokenEndpoint
) {}
