package com.academy.springsecurity6full.infrastructure.repository.mapper;

import com.academy.springsecurity6full.infrastructure.repository.entity.RegisteredClientEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * Mapper responsável pela conversão entre RegisteredClient e RegisteredClientEntity
 */
@Component
@Slf4j
public class RegisteredClientMapper {

    /**
     * Converte RegisteredClient para RegisteredClientEntity
     *
     * @param registeredClient objeto do domínio OAuth2
     * @return entidade JPA para persistência
     */
    public RegisteredClientEntity toEntity(RegisteredClient registeredClient) {
        if (registeredClient == null) {
            return null;
        }

        log.debug("Convertendo RegisteredClient para Entity: {}", registeredClient.getClientId());

        RegisteredClientEntity entity = new RegisteredClientEntity();

        // ===== CAMPOS BÁSICOS =====
        entity.setId(registeredClient.getId());
        entity.setClientId(registeredClient.getClientId());
        entity.setClientName(registeredClient.getClientName());
        entity.setClientSecret(registeredClient.getClientSecret());
        entity.setClientSecretExpiresAt(registeredClient.getClientSecretExpiresAt());

        // ===== COLLECTIONS =====
        mapCollections(registeredClient, entity);

        // ===== TOKEN SETTINGS =====
        mapTokenSettings(registeredClient, entity);

        // ===== CLIENT SETTINGS =====
        mapClientSettings(registeredClient, entity);

        log.debug("Conversão para Entity concluída: {}", entity.getClientId());
        return entity;
    }

    /**
     * Converte RegisteredClientEntity para RegisteredClient
     *
     * @param entity entidade JPA
     * @return objeto do domínio OAuth2
     */
    public RegisteredClient toRegisteredClient(RegisteredClientEntity entity) {
        if (entity == null) {
            return null;
        }

        log.debug("Convertendo Entity para RegisteredClient: {}", entity.getClientId());

        RegisteredClient.Builder builder = RegisteredClient.withId(entity.getId())
                .clientId(entity.getClientId())
                .clientName(entity.getClientName())
                .clientSecret(entity.getClientSecret())
                .clientSecretExpiresAt(entity.getClientSecretExpiresAt());

        // ===== COLLECTIONS =====
        mapCollectionsFromEntity(entity, builder);

        // ===== TOKEN SETTINGS =====
        builder.tokenSettings(buildTokenSettings(entity));

        // ===== CLIENT SETTINGS =====
        builder.clientSettings(buildClientSettings(entity));

        RegisteredClient result = builder.build();
        log.debug("Conversão para RegisteredClient concluída: {}", result.getClientId());

        return result;
    }

    /**
     * Mapeia as collections do RegisteredClient para a Entity
     */
    private void mapCollections(RegisteredClient registeredClient, RegisteredClientEntity entity) {
        // Client Authentication Methods
        if (registeredClient.getClientAuthenticationMethods() != null) {
            entity.setClientAuthenticationMethods(
                    registeredClient.getClientAuthenticationMethods().stream()
                            .map(ClientAuthenticationMethod::getValue)
                            .collect(Collectors.toSet())
            );
        }

        // Authorization Grant Types
        if (registeredClient.getAuthorizationGrantTypes() != null) {
            entity.setAuthorizationGrantTypes(
                    registeredClient.getAuthorizationGrantTypes().stream()
                            .map(AuthorizationGrantType::getValue)
                            .collect(Collectors.toSet())
            );
        }

        // URIs e Scopes
        entity.setRedirectUris(registeredClient.getRedirectUris());
        entity.setPostLogoutRedirectUris(registeredClient.getPostLogoutRedirectUris());
        entity.setScopes(registeredClient.getScopes());
    }

    /**
     * Mapeia as collections da Entity para o RegisteredClient Builder
     */
    private void mapCollectionsFromEntity(RegisteredClientEntity entity, RegisteredClient.Builder builder) {
        // Client Authentication Methods
        if (entity.getClientAuthenticationMethods() != null) {
            builder.clientAuthenticationMethods(methods ->
                    entity.getClientAuthenticationMethods().forEach(method ->
                            methods.add(new ClientAuthenticationMethod(method))
                    )
            );
        }

        // Authorization Grant Types
        if (entity.getAuthorizationGrantTypes() != null) {
            builder.authorizationGrantTypes(grants ->
                    entity.getAuthorizationGrantTypes().forEach(grant ->
                            grants.add(new AuthorizationGrantType(grant))
                    )
            );
        }

        // Redirect URIs
        if (entity.getRedirectUris() != null && !entity.getRedirectUris().isEmpty()) {
            builder.redirectUris(uris -> uris.addAll(entity.getRedirectUris()));
        }

        // Post Logout Redirect URIs
        if (entity.getPostLogoutRedirectUris() != null && !entity.getPostLogoutRedirectUris().isEmpty()) {
            builder.postLogoutRedirectUris(uris -> uris.addAll(entity.getPostLogoutRedirectUris()));
        }

        // Scopes
        if (entity.getScopes() != null && !entity.getScopes().isEmpty()) {
            builder.scopes(scopes -> scopes.addAll(entity.getScopes()));
        }
    }

    /**
     * Mapeia TokenSettings do RegisteredClient para a Entity
     */
    private void mapTokenSettings(RegisteredClient registeredClient, RegisteredClientEntity entity) {
        if (registeredClient.getTokenSettings() == null) {
            return;
        }

        TokenSettings tokenSettings = registeredClient.getTokenSettings();

        // Access Token Time To Live
        if (tokenSettings.getAccessTokenTimeToLive() != null) {
            entity.setAccessTokenTimeToLive(tokenSettings.getAccessTokenTimeToLive());
        }

        // Refresh Token Time To Live
        if (tokenSettings.getRefreshTokenTimeToLive() != null) {
            entity.setRefreshTokenTimeToLive(tokenSettings.getRefreshTokenTimeToLive());
        }

        // Reuse Refresh Tokens
        entity.setReuseRefreshTokens(tokenSettings.isReuseRefreshTokens());

        // ID Token Signature Algorithm
        if (tokenSettings.getIdTokenSignatureAlgorithm() != null) {
            entity.setIdTokenSignatureAlgorithm(
                    tokenSettings.getIdTokenSignatureAlgorithm().getName()
            );
        }
    }

    /**
     * Constrói TokenSettings a partir da Entity
     */
    private TokenSettings buildTokenSettings(RegisteredClientEntity entity) {
        TokenSettings.Builder builder = TokenSettings.builder();

        // Access Token Time To Live
        if (entity.getAccessTokenTimeToLiveSeconds() != null) {
            builder.accessTokenTimeToLive(entity.getAccessTokenTimeToLive());
        }

        // Refresh Token Time To Live
        if (entity.getRefreshTokenTimeToLiveSeconds() != null) {
            builder.refreshTokenTimeToLive(entity.getRefreshTokenTimeToLive());
        }

        // Reuse Refresh Tokens
        if (entity.getReuseRefreshTokens() != null) {
            builder.reuseRefreshTokens(entity.getReuseRefreshTokens());
        }

        // ID Token Signature Algorithm
        if (entity.getIdTokenSignatureAlgorithm() != null) {
            try {
                builder.idTokenSignatureAlgorithm(
                        SignatureAlgorithm.from(entity.getIdTokenSignatureAlgorithm())
                );
            } catch (Exception e) {
                log.warn("Algoritmo de assinatura inválido: {}", entity.getIdTokenSignatureAlgorithm(), e);
            }
        }

        return builder.build();
    }

    /**
     * Mapeia ClientSettings do RegisteredClient para a Entity
     */
    private void mapClientSettings(RegisteredClient registeredClient, RegisteredClientEntity entity) {
        if (registeredClient.getClientSettings() == null) {
            return;
        }

        ClientSettings clientSettings = registeredClient.getClientSettings();

        // Require Authorization Consent
        entity.setRequireAuthorizationConsent(clientSettings.isRequireAuthorizationConsent());

        // Require Proof Key
        entity.setRequireProofKey(clientSettings.isRequireProofKey());

        // JWK Set URL
        entity.setJwkSetUrl(clientSettings.getJwkSetUrl());

        // Token Endpoint Authentication Signing Algorithm
        if (clientSettings.getTokenEndpointAuthenticationSigningAlgorithm() != null) {
            entity.setTokenEndpointAuthenticationSigningAlgorithm(
                    clientSettings.getTokenEndpointAuthenticationSigningAlgorithm().getName()
            );
        }
    }

    /**
     * Constrói ClientSettings a partir da Entity
     */
    private ClientSettings buildClientSettings(RegisteredClientEntity entity) {
        ClientSettings.Builder builder = ClientSettings.builder();

        // Require Authorization Consent
        if (entity.getRequireAuthorizationConsent() != null) {
            builder.requireAuthorizationConsent(entity.getRequireAuthorizationConsent());
        }

        // Require Proof Key
        if (entity.getRequireProofKey() != null) {
            builder.requireProofKey(entity.getRequireProofKey());
        }

        // JWK Set URL
        if (entity.getJwkSetUrl() != null && !entity.getJwkSetUrl().trim().isEmpty()) {
            builder.jwkSetUrl(entity.getJwkSetUrl());
        }

        // Token Endpoint Authentication Signing Algorithm
        if (entity.getTokenEndpointAuthenticationSigningAlgorithm() != null) {
            try {
                builder.tokenEndpointAuthenticationSigningAlgorithm(
                        SignatureAlgorithm.from(entity.getTokenEndpointAuthenticationSigningAlgorithm())
                );
            } catch (Exception e) {
                log.warn("Algoritmo de assinatura inválido: {}",
                        entity.getTokenEndpointAuthenticationSigningAlgorithm(), e);
            }
        }

        return builder.build();
    }
}
