package com.academy.springsecurity6full.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;


@Slf4j
@Controller
public class OAuth2ClientController {

    @Value("${oauth2.authorization-server.base-url:http://localhost:8080}")
    private String authorizationServerUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // Configurações dos clientes (normalmente viriam de properties/database)
    private final Map<String, ClientConfig> clientConfigs = Map.of(
            "web-app", new ClientConfig("web-app", "web-secret", "http://localhost:8081/callback"),
            "mobile-app", new ClientConfig("mobile-app", "mobile-secret", "com.academy.app://callback"),
            "dashboard-app", new ClientConfig("dashboard-app", "dashboard-secret", "http://localhost:8082/callback"),
            "reports-app", new ClientConfig("reports-app", "reports-secret", "http://localhost:8083/callback")
    );

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String client_id,
                        @RequestParam(required = false) String scope) {

        // Se não especificou cliente, redireciona para seleção
        if (client_id == null) {
            return "redirect:/";
        }

        ClientConfig config = clientConfigs.get(client_id);
        if (config == null) {
            log.error("Cliente não encontrado: {}", client_id);
            return "redirect:/?error=invalid_client";
        }

        // Gerar state para segurança
        String state = generateRandomState();

        // Construir URL de autorização
        String authUrl = String.format(
                "%s/oauth2/authorize?client_id=%s&response_type=code&scope=%s&redirect_uri=%s&state=%s",
                authorizationServerUrl,
                client_id,
                scope != null ? scope : "openid profile",
                config.getRedirectUri(),
                state
        );

        log.info("Redirecionando para: {}", authUrl);
        return "redirect:" + authUrl;
    }

    @GetMapping("/callback")
    public String callback(@RequestParam(required = false) String code,
                           @RequestParam(required = false) String state,
                           @RequestParam(required = false) String error,
                           @RequestParam(required = false) String error_description,
                           @RequestParam(required = false) String client_id,
                           Model model) {

        log.info("Callback recebido - Code: {}, State: {}, Error: {}",
                code != null ? "***" : null, state, error);

        // Verificar se houve erro na autorização
        if (error != null) {
            model.addAttribute("error", "Erro na autorização: " + error +
                    (error_description != null ? " - " + error_description : ""));
            return "result";
        }

        // Verificar se recebeu o código
        if (code == null) {
            model.addAttribute("error", "Authorization Code não recebido");
            return "result";
        }

        // Adicionar informações básicas ao model
        model.addAttribute("authorizationCode", code);
        model.addAttribute("state", state);

        // Determinar qual cliente usar (pode vir de parâmetro ou session)
        String clientId = determineClientId(client_id);
        ClientConfig config = clientConfigs.get(clientId);

        if (config == null) {
            model.addAttribute("error", "Configuração do cliente não encontrada: " + clientId);
            return "result";
        }

        try {
            // Trocar authorization code por tokens
            Map<String, Object> tokenResponse = exchangeCodeForTokens(code, config);

            if (tokenResponse != null) {
                model.addAttribute("tokenResponse", tokenResponse);
                model.addAttribute("success", true);

                // Log para debug
                log.info("Tokens obtidos com sucesso para cliente: {}", clientId);
                if (tokenResponse.containsKey("access_token")) {
                    log.info("Access Token recebido (primeiros 50 chars): {}",
                            tokenResponse.get("access_token").toString().substring(0,
                                    Math.min(50, tokenResponse.get("access_token").toString().length())));
                }

            } else {
                model.addAttribute("error", "Falha ao obter tokens do Authorization Server");
            }

        } catch (Exception e) {
            log.error("Erro ao trocar código por tokens", e);
            model.addAttribute("error", "Erro interno: " + e.getMessage());
        }

        return "result";
    }

    private Map<String, Object> exchangeCodeForTokens(String code, ClientConfig config) {
        try {
            // Preparar headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Autenticação Basic com credenciais do cliente
            String credentials = config.getClientId() + ":" + config.getClientSecret();
            String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());
            headers.set("Authorization", "Basic " + encodedCredentials);

            // Preparar body da requisição
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", "authorization_code");
            body.add("code", code);
            body.add("redirect_uri", config.getRedirectUri());

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

            // Fazer requisição para o endpoint de token
            String tokenUrl = authorizationServerUrl + "/oauth2/token";
            log.info("Fazendo requisição para: {}", tokenUrl);
            log.info("Client ID: {}", config.getClientId());
            log.info("Redirect URI: {}", config.getRedirectUri());

            ResponseEntity<Map> response = restTemplate.exchange(
                    tokenUrl,
                    HttpMethod.POST,
                    request,
                    Map.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                @SuppressWarnings("unchecked")
                Map<String, Object> tokenData = response.getBody();
                log.info("Resposta do token server: {}", tokenData.keySet());
                return tokenData;
            } else {
                log.error("Erro na resposta do token server: {}", response.getStatusCode());
                return null;
            }

        } catch (Exception e) {
            log.error("Exceção ao trocar código por tokens", e);
            throw new RuntimeException("Falha na troca de código por tokens", e);
        }
    }

    private String determineClientId(String paramClientId) {
        // Lógica para determinar o cliente
        // Pode usar parâmetro, session, ou padrão
        if (paramClientId != null && clientConfigs.containsKey(paramClientId)) {
            return paramClientId;
        }

        // Padrão
        return "web-app";
    }

    private String generateRandomState() {
        return java.util.UUID.randomUUID().toString();
    }

    // Classe interna para configuração do cliente
    private static class ClientConfig {
        private final String clientId;
        private final String clientSecret;
        private final String redirectUri;

        public ClientConfig(String clientId, String clientSecret, String redirectUri) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.redirectUri = redirectUri;
        }

        public String getClientId() { return clientId; }
        public String getClientSecret() { return clientSecret; }
        public String getRedirectUri() { return redirectUri; }
    }

    // Endpoint adicional para testar diferentes clientes
    @GetMapping("/test-client")
    public String testClient(@RequestParam String clientId,
                             @RequestParam String scopes,
                             Model model) {

        if (!clientConfigs.containsKey(clientId)) {
            model.addAttribute("error", "Cliente não encontrado: " + clientId);
            return "result";
        }

        // Redirecionar para o fluxo OAuth2
        return "redirect:/login?client_id=" + clientId + "&scope=" + scopes;
    }
}
