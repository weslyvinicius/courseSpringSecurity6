package com.academy.springsecurity6full.infrastructure.repository.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "tb_registered_client")
@Data
public class RegisteredClientEntity {

    @Id
    @Column(name = "id", length = 100)
    private String id;

    @Column(name = "client_id", length = 100, nullable = false, unique = true)
    private String clientId;

    @Column(name = "client_secret", length = 200)
    private String clientSecret;

    @Column(name = "client_name", length = 200)
    private String clientName;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_client_authentication_methods", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "authentication_method")
    private Set<String> clientAuthenticationMethods;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_authorization_grant_types", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "authorization_grant_type")
    private Set<String> authorizationGrantTypes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_redirect_uris", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "redirect_uri", length = 1000)
    private Set<String> redirectUris;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_post_logout_redirect_uris", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "post_logout_redirect_uri", length = 1000)
    private Set<String> postLogoutRedirectUris;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "tb_scopes", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "scope")
    private Set<String> scopes;

    @Column(name = "client_secret_expires_at")
    private Instant clientSecretExpiresAt;

    // ===== TOKEN SETTINGS =====
    @Column(name = "access_token_time_to_live")
    private Long accessTokenTimeToLiveSeconds;

    @Column(name = "refresh_token_time_to_live")
    private Long refreshTokenTimeToLiveSeconds;

    @Column(name = "reuse_refresh_tokens")
    private Boolean reuseRefreshTokens;

    @Column(name = "id_token_signature_algorithm", length = 100)
    private String idTokenSignatureAlgorithm;

    // ===== CLIENT SETTINGS =====
    @Column(name = "require_authorization_consent")
    private Boolean requireAuthorizationConsent;

    @Column(name = "require_proof_key")
    private Boolean requireProofKey;

    @Column(name = "jwt_set_url", length = 500)
    private String jwkSetUrl;

    @Column(name = "token_endpoint_auth_signing_algorithm", length = 100)
    private String tokenEndpointAuthenticationSigningAlgorithm;

    // ===== AUDIT FIELDS =====
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();

        // Valores padrão para evitar null
        if (reuseRefreshTokens == null) {
            reuseRefreshTokens = true;
        }
        if (requireAuthorizationConsent == null) {
            requireAuthorizationConsent = false;
        }
        if (requireProofKey == null) {
            requireProofKey = false;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    // ===== MÉTODOS AUXILIARES =====

    /**
     * Converte segundos para Duration (Access Token)
     */
    public Duration getAccessTokenTimeToLive() {
        return accessTokenTimeToLiveSeconds != null ?
                Duration.ofSeconds(accessTokenTimeToLiveSeconds) : Duration.ofHours(1);
    }

    /**
     * Define Duration em segundos (Access Token)
     */
    public void setAccessTokenTimeToLive(Duration duration) {
        this.accessTokenTimeToLiveSeconds = duration != null ? duration.getSeconds() : null;
    }

    /**
     * Converte segundos para Duration (Refresh Token)
     */
    public Duration getRefreshTokenTimeToLive() {
        return refreshTokenTimeToLiveSeconds != null ?
                Duration.ofSeconds(refreshTokenTimeToLiveSeconds) : Duration.ofHours(24);
    }

    /**
     * Define Duration em segundos (Refresh Token)
     */
    public void setRefreshTokenTimeToLive(Duration duration) {
        this.refreshTokenTimeToLiveSeconds = duration != null ? duration.getSeconds() : null;
    }
}