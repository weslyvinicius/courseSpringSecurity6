package com.academy.springsecurity6full.application.usecases;

import com.academy.springsecurity6full.application.dto.RefreshTokenRequestDTO;
import com.academy.springsecurity6full.application.dto.TokenResponseDTO;
import com.academy.springsecurity6full.domain.oauth.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenUseCase {

    private final OAuth2Service oAuth2Service;

    public TokenResponseDTO execute(RefreshTokenRequestDTO request) {
        var tokenResponse = oAuth2Service.refreshToken(
                request.tokenEndpoint(),
                request.clientId(),
                request.clientSecret(),
                request.refreshToken(),
                request.grantType(),
                request.scope()
        );

        return TokenResponseDTO.fromDomain(tokenResponse);
    }
}
