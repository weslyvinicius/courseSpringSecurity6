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
        log.info("Flow type - PKCE: {}, Traditional: {}",
                request.isPKCE(), request.isTraditionalFlow());

        // Gerar um estado único e salvar os dados da requisição
        if (request.state() == null || request.state().isEmpty()) {
            request = request.cloneAuthorizeRequestDTO(UUID.randomUUID().toString());
            log.info("Generated new state: {}", request.state());
        }

        // Salvar dados da requisição no state store
        stateStore.save(request.state(), request);

        // Gerar URL de autorização
        var authUrl = authorizeUseCase.execute(request);
        log.info("Redirecting to authorization URL: {}", authUrl);

        return "redirect:" + authUrl;
    }

    @PostMapping("/oauth2/token")
    public String exchangeToken(@ModelAttribute TokenRequestDTO request, Model model ) {
        log.info("Exchanging token with request: {}", request);
        log.info("Flow type - PKCE: {}, Traditional: {}",
                request.isPKCE(), request.isTraditionalFlow());

        try {
            TokenResponseDTO token = exchangeCodeForTokenUseCase.execute(request);
            log.info("Token exchange successful: {}", token);

            // Adicionar dados de sucesso ao modelo
            populateSuccessModel(model, token, request);

        } catch (IllegalArgumentException e) {
            log.error("Validation error exchanging token: ", e);
            populateErrorModel(model, "Validation Error: " + e.getMessage(), request);
        } catch (Exception e) {
            log.error("Error exchanging token: ", e);
            populateErrorModel(model, e.getMessage(), request);
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

        // Recuperar dados da requisição original usando o estado
        AuthorizeRequestDTO originalRequest = stateStore.get(state);
        if (originalRequest == null) {
            log.error("No stored request found for state: {}", state);
            model.addAttribute("error", "Invalid or expired state parameter");
            return "error";
        }

        // Adicionar dados da requisição original ao modelo
        populateCallbackModel(model, originalRequest);

        // Indicar tipo de fluxo no modelo
        model.addAttribute("isPKCE", originalRequest.isPKCE());
        model.addAttribute("isTraditional", originalRequest.isTraditionalFlow());

        return "result";
    }

    private void populateSuccessModel(Model model, TokenResponseDTO token, TokenRequestDTO request) {
        model.addAttribute("tokenResponse", token);
        model.addAttribute("success", true);
        model.addAttribute("flowType", request.isPKCE() ? "authorization_code_pkce" : "authorization_code");
        model.addAttribute("authorizationCode", request.code());
        model.addAttribute("clientId", request.clientId());
        model.addAttribute("redirectUri", request.redirectUri());
        model.addAttribute("isPKCE", request.isPKCE());
    }

    private void populateErrorModel(Model model, String errorMessage, TokenRequestDTO request) {
        model.addAttribute("error", errorMessage);
        model.addAttribute("authorizationCode", request.code());
        model.addAttribute("clientId", request.clientId());
        model.addAttribute("redirectUri", request.redirectUri());
        model.addAttribute("tokenEndpoint", request.tokenEndpoint());
        model.addAttribute("isPKCE", request.isPKCE());
    }

    private void populateCallbackModel(Model model, AuthorizeRequestDTO request) {
        model.addAttribute("clientId", request.clientId());
        model.addAttribute("redirectUri", request.redirectUri());
        model.addAttribute("tokenEndpoint", request.tokenEndpoint());

        // Determinar se é PKCE ou tradicional
        boolean isPKCE = request.isPKCE();
        model.addAttribute("isPKCE", isPKCE);

        log.info("Populating callback model - isPKCE: {}, codeVerifier present: {}",
                isPKCE, request.codeVerifier() != null);

        // Para fluxo tradicional, incluir client_secret
        if (!isPKCE) {
            model.addAttribute("clientSecret", request.clientSecret());
            log.info("Traditional flow - client_secret added to model");
        }

        // Para PKCE, incluir code_verifier
        if (isPKCE) {
            model.addAttribute("codeVerifier", request.codeVerifier());
            log.info("PKCE flow - codeVerifier added to model: {}",
                    request.codeVerifier() != null ? "present" : "null");
        }
    }

}