package com.academy.springsecurity6full.controller;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

/**
 * Controller para demonstrar o fluxo Client Credentials Grant
 *
 * Client Credentials é usado para autenticação de aplicação para aplicação (machine-to-machine)
 * Não envolve usuários finais - apenas as credenciais do cliente (client_id + client_secret)
 */
@Controller
@RequestMapping("/client-credentials")
public class ClientCredentialsController {

    // Configurações que devem coincidir com seu Authorization Server
    private static final String AUTHORIZATION_SERVER_URL = "http://localhost:8080";

    // Clientes configurados no seu SecurityConfig
    private static final String API_CLIENT_ID = "api-client";
    private static final String API_CLIENT_SECRET = "api-secret";

    private static final String TEST_CLIENT_ID = "test-client";
    private static final String TEST_CLIENT_SECRET = "test-secret";

    /**
     * Página inicial para demonstração do Client Credentials
     */
    @GetMapping
    public String home() {
        return "index";
    }

    /**
     * Endpoint para obter token usando Client Credentials Grant
     */
    @PostMapping("/get-token")
    public String getToken(@RequestParam String clientId,
                           @RequestParam String clientSecret,
                           @RequestParam(required = false) String scopes,
                           Model model) {

        try {
            // Fazer requisição para obter token
            Map<String, Object> tokenResponse = requestAccessToken(clientId, clientSecret, scopes);

            model.addAttribute("success", true);
            model.addAttribute("tokenResponse", tokenResponse);
            model.addAttribute("clientId", clientId);
            model.addAttribute("scopes", scopes);

        } catch (Exception e) {
            model.addAttribute("error", "Erro ao obter token: " + e.getMessage());
            model.addAttribute("clientId", clientId);
            model.addAttribute("scopes", scopes);
        }

        return "result";
    }

    /**
     * Endpoint para testar o token obtido
     */
    @PostMapping("/test-token")
    public String testToken(@RequestParam String accessToken, Model model) {
        try {
            // Simular uma chamada de API usando o token
            Map<String, Object> apiResponse = callProtectedApi(accessToken);

            model.addAttribute("apiSuccess", true);
            model.addAttribute("apiResponse", apiResponse);
            model.addAttribute("accessToken", accessToken);

        } catch (Exception e) {
            model.addAttribute("apiError", "Erro ao chamar API: " + e.getMessage());
            model.addAttribute("accessToken", accessToken);
        }

        return "api-result";
    }

    /**
     * Método privado para fazer a requisição de token usando Client Credentials Grant
     */
    private Map<String, Object> requestAccessToken(String clientId, String clientSecret, String scopes) {
        RestTemplate restTemplate = new RestTemplate();

        // Configurar cabeçalhos
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Autenticação Basic (CLIENT_ID:CLIENT_SECRET em Base64)
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);

        // Corpo da requisição para Client Credentials Grant
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "client_credentials");

        // Adicionar escopos se fornecidos
        if (scopes != null && !scopes.trim().isEmpty()) {
            body.add("scope", scopes.trim());
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        // Fazer requisição para o endpoint /oauth2/token
        String tokenUrl = AUTHORIZATION_SERVER_URL + "/oauth2/token";
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        return response.getBody();
    }

    /**
     * Método para simular chamada a uma API protegida usando o access token
     * (Este é apenas um exemplo - você substituiria pela sua API real)
     */
    private Map<String, Object> callProtectedApi(String accessToken) {
        // Em um cenário real, você faria uma chamada para sua API protegida
        // Exemplo: GET /api/protected-resource
        // Authorization: Bearer {accessToken}

        // Para demonstração, vamos simular uma resposta
        return Map.of(
                "message", "Acesso autorizado com sucesso!",
                "timestamp", System.currentTimeMillis(),
                "resource", "dados-protegidos",
                "token_used", accessToken.substring(0, Math.min(20, accessToken.length())) + "..."
        );
    }

    /**
     * Endpoint para mostrar informações sobre os clientes disponíveis
     */
    @GetMapping("/clients-info")
    public String clientsInfo(Model model) {
        // Informações sobre os clientes configurados
        model.addAttribute("apiClient", Map.of(
                "id", API_CLIENT_ID,
                "secret", API_CLIENT_SECRET,
                "scopes", "read, write, admin"
        ));

        model.addAttribute("testClient", Map.of(
                "id", TEST_CLIENT_ID,
                "secret", TEST_CLIENT_SECRET,
                "scopes", "test"
        ));

        return "clients-info";
    }
}