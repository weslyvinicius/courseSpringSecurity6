package com.academy.springsecurity6full.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationContext;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationException;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AuthorizationCodeRequestAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;

import java.util.function.Consumer;

/**
 * Validador de redirect_uri baseado na documentação oficial:
 * - Compara a URI solicitada exatamente com uma das URIs registradas no RegisteredClient.
 * - Se não houver match exato, lança INVALID_REQUEST.
 */

@Slf4j
public class CustomRedirectUriValidator implements Consumer<OAuth2AuthorizationCodeRequestAuthenticationContext> {

    @Override
    public void accept(OAuth2AuthorizationCodeRequestAuthenticationContext authenticationContext) {
        OAuth2AuthorizationCodeRequestAuthenticationToken authRequest =
                authenticationContext.getAuthentication();

        RegisteredClient registeredClient = authenticationContext.getRegisteredClient();
        String requestedRedirectUri = authRequest.getRedirectUri();

        log.info("Validating redirect_uri: {}, registered URIs: {}",
                requestedRedirectUri, registeredClient.getRedirectUris());

        // Recomendação oficial: matching EXATO com as URIs pré-registradas
        if (!registeredClient.getRedirectUris().contains(requestedRedirectUri)) {
            OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_REQUEST);
            log.info("Invalid redirect_uri: {} not in registered URIs", requestedRedirectUri);

            throw new OAuth2AuthorizationCodeRequestAuthenticationException(error, null);
        }
    }

}
