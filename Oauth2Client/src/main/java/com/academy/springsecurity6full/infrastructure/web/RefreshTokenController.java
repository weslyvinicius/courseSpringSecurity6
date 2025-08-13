package com.academy.springsecurity6full.infrastructure.web;

import com.academy.springsecurity6full.application.dto.ClientDTO;
import com.academy.springsecurity6full.application.dto.RefreshTokenRequestDTO;
import com.academy.springsecurity6full.application.dto.TokenResponseDTO;
import com.academy.springsecurity6full.application.usecases.GetClientsUseCase;
import com.academy.springsecurity6full.application.usecases.RefreshTokenUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/oauth2/token")
@RequiredArgsConstructor
public class RefreshTokenController {

    private final GetClientsUseCase getClientsUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    @GetMapping("/refresh-form")
    public String refreshForm(Model model) {
        List<ClientDTO> clients = getClientsUseCase.execute("refresh_token");
        model.addAttribute("clients", clients);
        return "refresh-token";
    }

    @PostMapping("/refresh")
    public String refreshToken(@ModelAttribute RefreshTokenRequestDTO request, Model model) {
        log.info("Refreshing token with request: {}", request);

        try {
            TokenResponseDTO newTokens = refreshTokenUseCase.execute(request);
            log.info("Token refresh successful: {}", newTokens);

            // Adicionar o token response como um Map para o template
            model.addAttribute("tokenResponse", newTokens.toMap());
            model.addAttribute("success", true);
            model.addAttribute("flowType", "refresh");

            // Manter dados do request para referência
            model.addAttribute("clientId", request.clientId());
            model.addAttribute("refreshToken", request.refreshToken());
            model.addAttribute("tokenEndpoint", request.tokenEndpoint());

        } catch (Exception e) {
            log.error("Error refreshing token: ", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("clientId", request.clientId());
            model.addAttribute("refreshToken", request.refreshToken());
            model.addAttribute("tokenEndpoint", request.tokenEndpoint());
            model.addAttribute("flowType", "refresh");
        }

        return "result";
    }
}
