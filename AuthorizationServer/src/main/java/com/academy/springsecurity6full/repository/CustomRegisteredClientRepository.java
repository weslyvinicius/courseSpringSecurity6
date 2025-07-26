package com.academy.springsecurity6full.repository;

import com.academy.springsecurity6full.repository.entity.RegisteredClientEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Repository;

import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class CustomRegisteredClientRepository implements RegisteredClientRepository {

    private final JpaRegisteredClientRepository jpaRepository;

    @Override
    public void save(RegisteredClient registeredClient) {
        RegisteredClientEntity entity = toEntity(registeredClient);
        jpaRepository.save(entity);
    }

    @Override
    public RegisteredClient findById(String id) {
        return jpaRepository.findById(id)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        return jpaRepository.findByClientId(clientId)
                .map(this::toRegisteredClient)
                .orElse(null);
    }

    public void deleteById(String id) {
        jpaRepository.deleteById(id);
    }

    private RegisteredClientEntity toEntity(RegisteredClient registeredClient) {
        RegisteredClientEntity rc = new RegisteredClientEntity();
        rc.setId(registeredClient.getId());
        rc.setClientId(registeredClient.getClientId());
        rc.setClientName(registeredClient.getClientName());
        rc.setClientSecret(registeredClient.getClientSecret());
        rc.setClientAuthenticationMethods(
                        registeredClient.getClientAuthenticationMethods().stream()
                                .map(ClientAuthenticationMethod::getValue)
                                .collect(Collectors.toSet())
                );
        rc.setAuthorizationGrantTypes(
                        registeredClient.getAuthorizationGrantTypes().stream()
                                .map(AuthorizationGrantType::getValue)
                                .collect(Collectors.toSet())
                );
        rc.setRedirectUris(registeredClient.getRedirectUris());
        rc.setPostLogoutRedirectUris(registeredClient.getPostLogoutRedirectUris());
        rc.setScopes(registeredClient.getScopes());
        rc.setClientSecretExpiresAt(registeredClient.getClientSecretExpiresAt());
        return rc;
    }

    private RegisteredClient toRegisteredClient(RegisteredClientEntity entity) {
        return RegisteredClient.withId(entity.getId())
                .clientId(entity.getClientId())
                .clientName(entity.getClientName())
                .clientSecret(entity.getClientSecret())
                .clientAuthenticationMethods(methods -> entity.getClientAuthenticationMethods().forEach(method ->
                        methods.add(new ClientAuthenticationMethod(method))
                ))
                .authorizationGrantTypes(grants -> entity.getAuthorizationGrantTypes().forEach(grant ->
                        grants.add(new AuthorizationGrantType(grant))
                ))
                .redirectUris(uris -> uris.addAll(entity.getRedirectUris()))
                .postLogoutRedirectUris(uris -> {
                    if (entity.getPostLogoutRedirectUris() != null) {
                        uris.addAll(entity.getPostLogoutRedirectUris());
                    }
                })
                .scopes(scopes -> scopes.addAll(entity.getScopes()))
                .clientSecretExpiresAt(entity.getClientSecretExpiresAt())
                .build();
    }
}
