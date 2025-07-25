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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

@Controller
public class OAuth2ClientController {

    // Configurações do cliente - devem coincidir com seu Authorization Server
    private static final String CLIENT_ID = "my-client";
    private static final String CLIENT_SECRET = "secret";
    private static final String AUTHORIZATION_SERVER_URL = "http://localhost:8080";
    private static final String REDIRECT_URI = "http://localhost:8081/callback";

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        // Construir URL de autorização
        String authorizationUrl = AUTHORIZATION_SERVER_URL + "/oauth2/authorize" +
                "?response_type=code" +
                "&client_id=" + CLIENT_ID +
                "&redirect_uri=" + REDIRECT_URI +
                "&scope=openid profile" +
                "&state=xyz123"; // Estado para prevenir CSRF

        return "redirect:" + authorizationUrl;
    }

    @GetMapping("/callback")
    public String callback(@RequestParam(required = false) String code,
                           @RequestParam(required = false) String error,
                           @RequestParam(required = false) String state,
                           Model model) {

        if (error != null) {
            model.addAttribute("error", "Erro na autorização: " + error);
            return "result";
        }

        if (code == null) {
            model.addAttribute("error", "Código de autorização não recebido");
            return "result";
        }

        // AQUI VOCÊ TEM O AUTHORIZATION CODE!
        model.addAttribute("authorizationCode", code);
        model.addAttribute("state", state);

        // Agora vamos trocar o código por um token
        try {
            Map<String, Object> tokenResponse = exchangeCodeForToken(code);
            model.addAttribute("tokenResponse", tokenResponse);
            model.addAttribute("success", true);
        } catch (Exception e) {
            model.addAttribute("error", "Erro ao trocar código por token: " + e.getMessage());
            model.addAttribute("authorizationCode", code); // Manter o código visível mesmo com erro
        }

        return "result";
    }

    private Map<String, Object> exchangeCodeForToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        // Cabeçalhos
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Autenticação básica (CLIENT_ID:CLIENT_SECRET em Base64)
        String auth = CLIENT_ID + ":" + CLIENT_SECRET;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);

        // Corpo da requisição
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "authorization_code");
        body.add("code", code);
        body.add("redirect_uri", REDIRECT_URI);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        // Fazer a requisição
        String tokenUrl = AUTHORIZATION_SERVER_URL + "/oauth2/token";
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        return response.getBody();
    }
}
