package com.academy.springsecurity6full.infrastructure.web;

import com.academy.springsecurity6full.application.dto.*;
import com.academy.springsecurity6full.application.usecases.AuthorizeUseCase;
import com.academy.springsecurity6full.application.usecases.ExchangeCodeForTokenUseCase;
import com.academy.springsecurity6full.application.usecases.GetClientsUseCase;
import com.academy.springsecurity6full.application.usecases.RefreshTokenUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/oauth2")
@RequiredArgsConstructor
public class OAuth2TestController {

    private final GetClientsUseCase getClientsUseCase;
    private final AuthorizeUseCase authorizeUseCase;
    private final ExchangeCodeForTokenUseCase exchangeCodeForTokenUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    @GetMapping("/clients/{grantType}")
    public List<ClientDTO> getClientsByGrantType(
            @PathVariable("grantType") String grantType) {
        return getClientsUseCase.execute(grantType);
    }

    @PostMapping("/authorize")
    public String authorize(@RequestBody AuthorizeRequestDTO request) {
        return authorizeUseCase.execute(request);
    }

    @PostMapping("/token")
    public TokenResponseDTO exchangeCodeForToken(@RequestBody TokenRequestDTO request) {
        return exchangeCodeForTokenUseCase.execute(request);
    }

    @PostMapping("/refresh")
    public TokenResponseDTO refreshToken(@RequestBody RefreshTokenRequestDTO request) {
        return refreshTokenUseCase.execute(request);
    }
}
