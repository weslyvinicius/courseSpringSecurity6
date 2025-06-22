package com.academy.springsecurity6full.controller;

import com.academy.springsecurity6full.dto.AuthRequest;
import com.academy.springsecurity6full.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthRequest loginRequest;

    private RegisterRequest registerRequest;


    @Test
    void login_success_returnsJwt() throws Exception {
        loginRequest = new AuthRequest("john", "j123456");
        // Act: Enviar requisição POST para /auth/login
        mockMvc.perform( post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                // Assert: Verificar status 200 e presença do token JWT
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").exists())
                .andExpect(jsonPath("$.jwt").isString());
    }

    @Test
    void login_invalidCredentials_returnsUnauthorized() throws Exception {
        // Arrange: Modificar senha para causar falha
        loginRequest = new AuthRequest("john", "wrongpass");

        // Act: Enviar requisição POST para /auth/login
        mockMvc.perform( post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                // Assert: Verificar status 401 (Unauthorized)
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    void register_newUser_success() throws Exception {
        registerRequest = new RegisterRequest("newuser", "testpass");

        // Act: Enviar requisição POST para /auth/register
        mockMvc.perform( post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON )
                        .content(objectMapper.writeValueAsString(registerRequest)))
                // Assert: Verificar status 200 e mensagem de sucesso
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jwt").exists())
                .andExpect(jsonPath("$.jwt").isString());
    }

    @Test
    void register_existingUser_returnsBadRequest() throws Exception {
        // Arrange: Usar o mesmo usuário criado no setUp
        // (john já existe no banco)
        registerRequest = new RegisterRequest("john", "j123456");

        // Act: Enviar requisição POST para /api/auth/register
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                // Assert: Verificar status 400 e mensagem de erro
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").value("Username already exists"));
    }

}