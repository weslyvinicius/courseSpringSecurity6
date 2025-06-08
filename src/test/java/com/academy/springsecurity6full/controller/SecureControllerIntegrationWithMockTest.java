package com.academy.springsecurity6full.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecureControllerIntegrationWithMockTest {

    @Autowired
    private MockMvc mockMvc;

    // Testa o endpoint /api/admin com usuário ROLE_ADMIN
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminEndpointWithRoleAdmin() throws Exception {
        mockMvc.perform(get("/api/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Admin access granted"));
    }

    // Testa falha no endpoint /api/admin com usuário sem ROLE_ADMIN
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testAdminEndpointAccessDenied() throws Exception {
        mockMvc.perform(get("/api/admin"))
                .andExpect(status().isForbidden());
    }

    // Testa o endpoint /api/manager-or-admin com usuário ROLE_MANAGER
    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testManagerOrAdminEndpointWithRoleManager() throws Exception {
        mockMvc.perform(get("/api/manager-or-admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Manager or Admin access granted"));
    }

    // Testa o endpoint /api/read com usuário com READ_RESOURCE
    @Test
    @WithMockUser(username = "john", authorities = {"READ_RESOURCE"})
    void testReadEndpointWithReadAuthority() throws Exception {
        mockMvc.perform(get("/api/read"))
                .andExpect(status().isOk())
                .andExpect(content().string("Read access granted"));
    }

    // Testa o endpoint /api/read-or-write com usuário com WRITE_RESOURCE
    @Test
    @WithMockUser(username = "john", authorities = {"WRITE_RESOURCE"})
    void testReadOrWriteEndpointWithWriteAuthority() throws Exception {
        mockMvc.perform(get("/api/read-or-write"))
                .andExpect(status().isOk())
                .andExpect(content().string("Read or Write access granted"));
    }

    // Testa o endpoint /api/user/{username} com usuário correspondente
    @Test
    @WithMockUser(username = "john")
    void testUserDataEndpointWithMatchingUser() throws Exception {
        mockMvc.perform(get("/api/user/john"))
                .andExpect(status().isOk())
                .andExpect(content().string("User data for john"));
    }

    // Testa falha no endpoint /api/user/{username} com usuário diferente
    @Test
    @WithMockUser(username = "john")
    void testUserDataEndpointAccessDenied() throws Exception {
        mockMvc.perform(get("/api/user/other"))
                .andExpect(status().isForbidden());
    }

    // Testa o endpoint /api/resource/{resourceId} com SpEL personalizado
    @Test
    @WithMockUser(username = "john")
    void testCustomResourceEndpointWithMatchingOwner() throws Exception {
        mockMvc.perform(get("/api/resource/john_resource"))
                .andExpect(status().isOk())
                .andExpect(content().string("Custom resource access for john_resource"));
    }
}