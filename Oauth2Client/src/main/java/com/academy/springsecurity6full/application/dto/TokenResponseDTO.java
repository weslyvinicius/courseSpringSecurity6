package com.academy.springsecurity6full.application.dto;

import com.academy.springsecurity6full.domain.oauth.TokenResponse;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

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

    /**
     * Converte o TokenResponseDTO em um Map para ser usado no template Thymeleaf
     * @return Map com os dados do token
     */
    public Map<String, Object> toMap() {
        Map<String, Object> tokenMap = new LinkedHashMap<>();

        if (accessToken != null) {
            tokenMap.put("access_token", accessToken);
        }
        if (tokenType != null) {
            tokenMap.put("token_type", tokenType);
        }
        if (expiresIn != null) {
            tokenMap.put("expires_in", expiresIn);
        }
        if (refreshToken != null) {
            tokenMap.put("refresh_token", refreshToken);
        }
        if (scope != null) {
            tokenMap.put("scope", scope);
        }
        if (createdAt != null) {
            tokenMap.put("created_at", createdAt.toString());
        }
        if (idToken != null) {
            tokenMap.put("id_token", idToken);
        }

        return tokenMap;
    }
}
