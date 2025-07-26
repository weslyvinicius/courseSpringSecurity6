package com.academy.springsecurity6full.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class RegisteredClientCreateDto {

    @NotBlank(message = "Client ID é obrigatório")
    private String clientId;

    @NotBlank(message = "Client Secret é obrigatório")
    private String clientSecret;

    private String clientName;

    @NotEmpty(message = "Pelo menos um método de autenticação é obrigatório")
    private Set<String> clientAuthenticationMethods;

    @NotEmpty(message = "Pelo menos um tipo de grant é obrigatório")
    private Set<String> authorizationGrantTypes;

    @NotEmpty(message = "Pelo menos uma URI de redirecionamento é obrigatória")
    private Set<String> redirectUris;

    private Set<String> postLogoutRedirectUris;

    @NotEmpty(message = "Pelo menos um escopo é obrigatório")
    private Set<String> scopes;
}
