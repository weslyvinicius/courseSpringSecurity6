package com.academy.springsecurity6full.infrastructure.web;

import com.academy.springsecurity6full.application.dto.*;
import com.academy.springsecurity6full.application.usecases.AuthorizeUseCase;
import com.academy.springsecurity6full.application.usecases.ExchangeCodeForTokenUseCase;
import com.academy.springsecurity6full.application.usecases.GetClientsUseCase;
import com.academy.springsecurity6full.application.usecases.OAuth2StateStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping
@RequiredArgsConstructor
public class OAuth2WebController {

    private final GetClientsUseCase getClientsUseCase;
    private final AuthorizeUseCase authorizeUseCase;
    private final ExchangeCodeForTokenUseCase exchangeCodeForTokenUseCase;
    private final OAuth2StateStore stateStore;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/configure")
    public String configure(Model model) {
        List<ClientDTO> clients = getClientsUseCase.execute("authorization_code");
        model.addAttribute("clients", clients);
        return "configure";
    }

    @PostMapping("/oauth2/authorize")
    public String authorize(@ModelAttribute AuthorizeRequestDTO request ) {
        log.info("Authorizing with request: {}", request);

        // Gerar um estado único e salvar os dados da requisição
        if (request.state() == null || request.state().isEmpty()) {
            request = request.cloneAuthorizeRequestDTO(UUID.randomUUID().toString());
            log.info("Generated new state: {}", request.state());
        }
        stateStore.save(request.state(), request);
        var authUrl = authorizeUseCase.execute(request);
        log.info("Redirecting to authorization URL: {}", authUrl);
        return "redirect:" + authUrl;
    }

    @PostMapping("/oauth2/token")
    public String exchangeToken(@ModelAttribute TokenRequestDTO request, Model model ) {
        log.info("Exchanging token with request: {}", request);

        try {
            TokenResponseDTO token = exchangeCodeForTokenUseCase.execute(request);
            log.info("Token exchange successful: {}", token);

            // Adicionar o token response como um Map para o template
            model.addAttribute("tokenResponse", token);
            model.addAttribute("success", true);
            model.addAttribute("flowType", "authorization_code");

            // Manter dados do request para referência
            model.addAttribute("authorizationCode", request.code());
            model.addAttribute("clientId", request.clientId());
            model.addAttribute("redirectUri", request.redirectUri());

        } catch (Exception e) {
            log.error("Error exchanging token: ", e);
            model.addAttribute("error", e.getMessage());
            model.addAttribute("authorizationCode", request.code());
            model.addAttribute("clientId", request.clientId());
            model.addAttribute("redirectUri", request.redirectUri());
            model.addAttribute("tokenEndpoint", request.tokenEndpoint());
        }
        return "result";
    }

    @GetMapping("/oauth2/callback")
    public String callback(
            @RequestParam("code") String code,
            @RequestParam(value = "state", required = false) String state,
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "error_description", required = false) String errorDescription,
            Model model
    ) {

        log.info("Received callback with parameters: code={}, state={}, error={}, error_description={}"
                , code, state, error, errorDescription);


        // Verificar se houve erro na autorização
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("errorDescription", errorDescription);
            return "error"; // página de erro
        }

        // Adicionar dados para o template result.html
        model.addAttribute("authorizationCode", code);
        model.addAttribute("state", state);

        // Recuperar os dados da requisição original usando o estado
        AuthorizeRequestDTO request = stateStore.get(state);
        model.addAttribute("clientId", request.clientId());
        model.addAttribute("clientSecret", request.clientSecret());
        model.addAttribute("redirectUri", request.redirectUri());
        model.addAttribute("tokenEndpoint", request.tokenEndpoint());

        return "result";
    }

}