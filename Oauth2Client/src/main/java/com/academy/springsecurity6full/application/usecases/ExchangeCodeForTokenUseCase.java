package com.academy.springsecurity6full.application.usecases;

import com.academy.springsecurity6full.application.dto.TokenRequestDTO;
import com.academy.springsecurity6full.application.dto.TokenResponseDTO;
import com.academy.springsecurity6full.domain.oauth.OAuth2Service;
import com.academy.springsecurity6full.domain.oauth.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExchangeCodeForTokenUseCase {

    private final OAuth2Service oAuth2Service;

    public TokenResponseDTO execute(TokenRequestDTO request) {
        log.info("Exchanging code for token - PKCE: {}, Traditional: {}",
                request.isPKCE(), request.isTraditionalFlow());

        var tokenResponse = request.isPKCE()
                ? exchangeWithPKCE(request)
                : exchangeTraditional(request);

        log.info("Token exchange successful using {} flow",
                request.isPKCE() ? "PKCE" : "traditional");

        return TokenResponseDTO.fromDomain(tokenResponse);
    }

    private TokenResponse exchangeWithPKCE(TokenRequestDTO request) {
        log.info("Using PKCE flow for token exchange");

        return oAuth2Service.exchangeCodeForTokenWithPKCE(
                request.tokenEndpoint(),
                request.clientId(),
                request.code(),
                request.redirectUri(),
                request.grantType(),
                request.codeVerifier()
        );
    }

    private TokenResponse exchangeTraditional(TokenRequestDTO request) {
        log.info("Using traditional flow for token exchange");

        if (request.clientSecret() == null || request.clientSecret().isEmpty()) {
            throw new IllegalArgumentException(
                    "Client secret is required for traditional authorization code flow");
        }

        return oAuth2Service.exchangeCodeForToken(
                request.tokenEndpoint(),
                request.clientId(),
                request.clientSecret(),
                request.code(),
                request.redirectUri(),
                request.grantType()
        );
    }
}