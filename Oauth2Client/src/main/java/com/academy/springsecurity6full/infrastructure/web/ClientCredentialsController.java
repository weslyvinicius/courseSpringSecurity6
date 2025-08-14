package com.academy.springsecurity6full.infrastructure.web;

import com.academy.springsecurity6full.application.dto.ClientCredentialsRequestDTO;
import com.academy.springsecurity6full.application.dto.ClientDTO;
import com.academy.springsecurity6full.application.dto.TokenResponseDTO;
import com.academy.springsecurity6full.application.usecases.ClientCredentialsUseCase;
import com.academy.springsecurity6full.application.usecases.GetClientsUseCase;
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
public class ClientCredentialsController {

    private final GetClientsUseCase getClientsUseCase;
    private final ClientCredentialsUseCase clientCredentialsUseCase;

    @GetMapping("/client-credentials-form")
    public String clientCredentialsForm(Model model) {
        List<ClientDTO> clients = getClientsUseCase.execute("client_credentials");
        model.addAttribute("clients", clients);
        return "client-credentials";
    }

    @PostMapping("/client-credentials")
    public String exchangeClientCredentials(@ModelAttribute ClientCredentialsRequestDTO request, Model model) {
        log.info("Requesting token with client credentials: {}", request);

        try {
            TokenResponseDTO tokenResponse = clientCredentialsUseCase.execute(request);
            log.info("Client credentials token exchange successful: {}", tokenResponse);

            // Adicionar o token response como um Map para o template
            model.addAttribute("tokenResponse", tokenResponse);
            model.addAttribute("success", true);
            model.addAttribute("flowType", "client_credentials");

            // Manter dados do request para referência
            model.addAttribute("clientId", request.clientId());
            model.addAttribute("scope", request.scope());
            model.addAttribute("tokenEndpoint", request.tokenEndpoint());

        } catch (Exception e) {
            log.error("Error requesting client credentials token: ", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("clientId", request.clientId());
            model.addAttribute("scope", request.scope());
            model.addAttribute("tokenEndpoint", request.tokenEndpoint());
            model.addAttribute("flowType", "client_credentials");
        }

        return "result";
    }
}
