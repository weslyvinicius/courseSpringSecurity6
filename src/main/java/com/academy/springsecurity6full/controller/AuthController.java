package com.academy.springsecurity6full.controller;

import com.academy.springsecurity6full.dto.AuthRequest;
import com.academy.springsecurity6full.dto.AuthResponse;
import com.academy.springsecurity6full.dto.RegisterRequest;
import com.academy.springsecurity6full.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para operações de autenticação.
 * <p>
 * Responsabilidades:
 * - Expor endpoints para login e registro
 * - Validar dados de entrada
 * - Retornar respostas padronizadas
 * - Tratar exceções de autenticação
 * <p>
 * Endpoints:
 * - POST /auth/login: Autentica usuário existente
 * - POST /auth/register: Registra novo usuário
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Endpoint para autenticação de usuário.
     * <p>
     * Recebe credenciais (username/password) e retorna token JWT
     * para acesso aos recursos protegidos da aplicação.
     *
     * @param AuthRequest Dados de login (username e password)
     * @return ResponseEntity com token JWT ou erro de autenticação
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        // Autentica o usuário e gera token JWT
        // O AuthService já retorna o token pronto
        String token = authService.authenticate(
                authRequest.username(),
                authRequest.password()
        );

        // Retorna token em formato padronizado
        return ResponseEntity.ok(new AuthResponse(token));


    }

    /**
     * Endpoint para registro de novo usuário.
     * <p>
     * Cria novo usuário no sistema com authorities padrão
     * e retorna token JWT para acesso imediato.
     *
     * @param registerRequest Dados de registro (username e password)
     * @return ResponseEntity com token JWT ou erro de registro
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        // Registra novo usuário e gera token JWT
        var token = authService.register(
                registerRequest.username(),
                registerRequest.password()
        );

        if (token.isPresent()) {
            // Se o token foi gerado com sucesso, retorna o token
            return ResponseEntity.ok(new AuthResponse(token.get()));
        }
        return ResponseEntity.badRequest().body("Username already exists");
    }
}