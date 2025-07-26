package com.academy.springsecurity6full.service;


import com.academy.springsecurity6full.controller.dto.RegisteredClientCreateDto;
import com.academy.springsecurity6full.controller.dto.RegisteredClientResponseDto;
import com.academy.springsecurity6full.repository.CustomRegisteredClientRepository;
import com.academy.springsecurity6full.repository.JpaRegisteredClientRepository;
import com.academy.springsecurity6full.repository.entity.RegisteredClientEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegisteredClientService {

    private final CustomRegisteredClientRepository registeredClientRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisteredClientResponseDto createRegisteredClient(RegisteredClientCreateDto dto) {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(dto.getClientId())
                .clientName(dto.getClientName())
                .clientSecret(passwordEncoder.encode(dto.getClientSecret()))
                .clientAuthenticationMethods(methods -> {
                    dto.getClientAuthenticationMethods().forEach(method ->
                            methods.add(new ClientAuthenticationMethod(method))
                    );
                })
                .authorizationGrantTypes(grants -> {
                    dto.getAuthorizationGrantTypes().forEach(grant ->
                            grants.add(new AuthorizationGrantType(grant))
                    );
                })
                .redirectUris(uris -> uris.addAll(dto.getRedirectUris()))
                .postLogoutRedirectUris(uris -> {
                    if (dto.getPostLogoutRedirectUris() != null) {
                        uris.addAll(dto.getPostLogoutRedirectUris());
                    }
                })
                .scopes(scopes -> scopes.addAll(dto.getScopes()))
                .build();

        registeredClientRepository.save(registeredClient);

        return toResponseDto(registeredClient);
    }

    public void deleteRegisteredClient(String id) {
        registeredClientRepository.deleteById(id);
    }

    private RegisteredClientResponseDto toResponseDto(RegisteredClient registeredClient) {
        RegisteredClientResponseDto dto = new RegisteredClientResponseDto();
        dto.setId(registeredClient.getId());
        dto.setClientId(registeredClient.getClientId());
        dto.setClientName(registeredClient.getClientName());
        dto.setClientAuthenticationMethods(
                registeredClient.getClientAuthenticationMethods().stream()
                        .map(ClientAuthenticationMethod::getValue)
                        .collect(Collectors.toSet())
        );
        dto.setAuthorizationGrantTypes(
                registeredClient.getAuthorizationGrantTypes().stream()
                        .map(AuthorizationGrantType::getValue)
                        .collect(Collectors.toSet())
        );
        dto.setRedirectUris(registeredClient.getRedirectUris());
        dto.setPostLogoutRedirectUris(registeredClient.getPostLogoutRedirectUris());
        dto.setScopes(registeredClient.getScopes());
        dto.setClientSecretExpiresAt(registeredClient.getClientSecretExpiresAt());
        // Note: createdAt e updatedAt serão preenchidos pela entidade
        return dto;
    }
}
