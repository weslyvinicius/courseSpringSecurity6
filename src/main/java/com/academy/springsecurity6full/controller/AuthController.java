package com.academy.springsecurity6full.controller;

import com.academy.springsecurity6full.config.ConflictException;
import com.academy.springsecurity6full.dto.AuthRequest;
import com.academy.springsecurity6full.dto.RefreshTokenRequest;
import com.academy.springsecurity6full.dto.RegisterRequest;
import com.academy.springsecurity6full.dto.TokenResponse;
import com.academy.springsecurity6full.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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
    public ResponseEntity<TokenResponse> login(@RequestBody AuthRequest authRequest) {
        try {
            TokenResponse tokenResponse = authService.authenticate(
                    authRequest.username(),
                    authRequest.password()
            );
            return ResponseEntity.ok(tokenResponse);
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
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
    public ResponseEntity<TokenResponse> register(@RequestBody RegisterRequest registerRequest) {
        try {
            TokenResponse tokenResponse = authService.register(
                    registerRequest.username(),
                    registerRequest.password()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(tokenResponse);
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    /**
     * Endpoint para renovar access token usando refresh token.
     *
     * @param refreshTokenRequest Objeto contendo o refresh token
     * @return ResponseEntity com novos tokens ou erro
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        try {
            TokenResponse tokenResponse = authService.refreshToken(refreshTokenRequest.refreshToken());
            return ResponseEntity.ok(tokenResponse);
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }
}