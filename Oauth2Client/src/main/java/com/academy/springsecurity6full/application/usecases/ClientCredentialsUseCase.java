package com.academy.springsecurity6full.application.usecases;

import com.academy.springsecurity6full.application.dto.ClientCredentialsRequestDTO;
import com.academy.springsecurity6full.application.dto.TokenResponseDTO;
import com.academy.springsecurity6full.domain.oauth.OAuth2Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClientCredentialsUseCase {

    private final OAuth2Service oAuth2Service;

    public TokenResponseDTO execute(ClientCredentialsRequestDTO request) {
        var tokenResponse = oAuth2Service.clientCredentials(
                request.tokenEndpoint(),
                request.clientId(),
                request.clientSecret(),
                request.grantType(),
                request.scope()
        );

        return TokenResponseDTO.fromDomain(tokenResponse);
    }
}
