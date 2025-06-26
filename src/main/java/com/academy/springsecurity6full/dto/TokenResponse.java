package com.academy.springsecurity6full.dto;

public record TokenResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
    public TokenResponse(String accessToken, String refreshToken, long expiresIn) {
        this(accessToken, refreshToken, "Bearer", expiresIn);
    }
}
