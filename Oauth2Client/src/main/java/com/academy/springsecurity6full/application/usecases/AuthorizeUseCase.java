package com.academy.springsecurity6full.application.usecases;

import com.academy.springsecurity6full.application.dto.AuthorizeRequestDTO;
import com.academy.springsecurity6full.domain.oauth.AuthorizationCodeFlow;
import org.springframework.stereotype.Component;

@Component
public class AuthorizeUseCase {

    public String execute(AuthorizeRequestDTO request) {
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow(
                request.clientId(),
                request.redirectUri(),
                request.responseType(),
                request.scope(),
                request.state()
        );

        return flow.buildAuthorizationUrl(request.authorizationEndpoint());
    }
}
