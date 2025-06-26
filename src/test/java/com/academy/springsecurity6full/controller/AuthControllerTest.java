package com.academy.springsecurity6full.controller;

import com.academy.springsecurity6full.domain.AuthRequest;
import com.academy.springsecurity6full.domain.RefreshTokenRequest;
import com.academy.springsecurity6full.domain.RegisterRequest;
import com.academy.springsecurity6full.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthRequest loginRequest;

    private RegisterRequest registerRequest;


    @Test
    void login_success_returnsTokenResponse() throws Exception {
        // Arrange
        loginRequest = new AuthRequest("john", "j123456");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").exists())
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

    @Test
    void login_invalidCredentials_returnsUnauthorized() throws Exception {
        // Arrange
        loginRequest = new AuthRequest("john", "wrongpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_nonExistentUser_returnsUnauthorized() throws Exception {
        // Arrange
        loginRequest = new AuthRequest("nonexistent", "password");

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void register_newUser_success() throws Exception {
        // Arrange
        registerRequest = new RegisterRequest("newuser", "testpass123");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").exists())
                .andExpect(jsonPath("$.expiresIn").isNumber());

    }

    @Test
    void register_existingUser_returnsConflict() throws Exception {
        // Arrange - Usar usuário que já existe (john)
        registerRequest = new RegisterRequest("john", "newpassword");

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    void refreshToken_validToken_returnsNewTokens() throws Exception {
        // Arrange - Primeiro fazer login para obter refresh token
        loginRequest = new AuthRequest("john", "j123456");

        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Extrair refresh token da resposta do login
        String loginResponse = loginResult.getResponse().getContentAsString();
        String refreshToken = objectMapper.readTree(loginResponse).get("refreshToken").asText();

        var refreshTokenRequest = new RefreshTokenRequest(refreshToken);

        // Act & Assert
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(refreshTokenRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.refreshToken").isString())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").exists())
                .andExpect(jsonPath("$.expiresIn").isNumber());
    }

}