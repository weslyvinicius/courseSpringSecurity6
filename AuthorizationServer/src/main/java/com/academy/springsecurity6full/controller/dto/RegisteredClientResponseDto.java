package com.academy.springsecurity6full.controller.dto;

import lombok.Data;

import java.time.Instant;
import java.util.Set;

@Data
public class RegisteredClientResponseDto {

    private String id;
    private String clientId;
    private String clientName;
    private Set<String> clientAuthenticationMethods;
    private Set<String> authorizationGrantTypes;
    private Set<String> redirectUris;
    private Set<String> postLogoutRedirectUris;
    private Set<String> scopes;
    private Instant clientSecretExpiresAt;
    private Instant createdAt;
    private Instant updatedAt;
}
