package com.academy.springsecurity6full.application.usecases;

import com.academy.springsecurity6full.application.dto.AuthorizeRequestDTO;
import com.academy.springsecurity6full.domain.oauth.AuthorizationCodeFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuthorizeUseCase {

    public String execute(AuthorizeRequestDTO request) {
        log.info("Executing authorization flow - PKCE: {}, Traditional: {}",
                request.isPKCE(), request.isTraditionalFlow());

        AuthorizationCodeFlow flow = new AuthorizationCodeFlow(
                request.clientId(),
                request.redirectUri(),
                request.responseType(),
                request.scope(),
                request.state(),
                request.codeChallenge(),
                request.codeChallengeMethod()
        );

        String authUrl = flow.buildAuthorizationUrl(request.authorizationEndpoint());

        if (flow.isPKCE()) {
            log.info("Generated PKCE authorization URL with code_challenge");
        } else {
            log.info("Generated traditional authorization URL");
        }

        return authUrl;
    }
}