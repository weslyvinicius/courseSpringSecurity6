package com.academy.springsecurity6full.application.dto;

public record TokenRequestDTO(
        String clientId,
        String clientSecret,
        String redirectUri,
        String grantType,
        String tokenEndpoint,
        String scope,
        String state,
        String code
) {}
