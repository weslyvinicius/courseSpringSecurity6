package com.academy.springsecurity6full.application.usecases;

import com.academy.springsecurity6full.application.dto.TokenRequestDTO;
import com.academy.springsecurity6full.application.dto.TokenResponseDTO;
import com.academy.springsecurity6full.domain.oauth.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangeCodeForTokenUseCase {

    private final OAuth2Service oAuth2Service;

    public TokenResponseDTO execute(TokenRequestDTO request) {
        var tokenResponse = oAuth2Service.exchangeCodeForToken(
                request.tokenEndpoint(),
                request.clientId(),
                request.clientSecret(),
                request.code(),
                request.redirectUri(),
                request.grantType()
        );

        return TokenResponseDTO.fromDomain(tokenResponse);
    }
}
