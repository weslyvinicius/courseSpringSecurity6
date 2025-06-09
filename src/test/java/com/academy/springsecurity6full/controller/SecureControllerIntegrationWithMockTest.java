package com.academy.springsecurity6full.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecureControllerIntegrationWithMockTest {

    @Autowired
    private MockMvc mockMvc;

    // Testa o endpoint /api/admin com usuário ROLE_ADMIN
    // @Secured verifica antes da execução, evitando chamada ao serviço se falhar
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminEndpointWithRoleAdmin() throws Exception {
        mockMvc.perform(get("/api/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Admin access: Resource content for admin_resource"));
    }

    // Testa falha no endpoint /api/admin com usuário sem ROLE_ADMIN
    // @Secured nega acesso antes da execução, sem chamar o serviço
    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    void testAdminEndpointAccessDenied() throws Exception {
        mockMvc.perform(get("/api/admin"))
                .andExpect(status().isForbidden());
    }

    // Testa o endpoint /api/resource/owner/{id} com usuário correspondente
    // @PostAuthorize verifica após a execução do serviço
    @Test
    @WithMockUser(username = "john")
    void testResourceOwnerEndpointWithMatchingUser() throws Exception {
        mockMvc.perform(get("/api/resource/owner/john_resource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Resource content for john_resource"));
    }

    // Testa falha no endpoint /api/resource/owner/{id} com usuário diferente
    // @PostAuthorize executa o serviço, mas nega acesso após verificar returnObject
    @Test
    @WithMockUser(username = "other")
    void testResourceOwnerEndpointAccessDenied() throws Exception {
        mockMvc.perform(get("/api/resource/owner/john_resource"))
                .andExpect(status().isForbidden());
    }

    // Testa o endpoint /api/resource/custom/{id} com usuário correspondente
    // @PostAuthorize verifica após a execução do serviço usando canAccessResource
    @Test
    @WithMockUser(username = "john")
    void testCustomResourceEndpointWithMatchingOwner() throws Exception {
        mockMvc.perform(get("/api/resource/custom/john_resource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Resource content for john_resource"));
    }

    // Testa falha no endpoint /api/resource/custom/{id} com usuário não correspondente
    // @PostAuthorize executa o serviço, mas nega acesso após verificação
    @Test
    @WithMockUser(username = "other")
    void testCustomResourceEndpointAccessDenied() throws Exception {
        mockMvc.perform(get("/api/resource/custom/john_resource"))
                .andExpect(status().isForbidden());
    }

    // Testa o endpoint /api/resource/pre/{id} com usuário correspondente
    // @PreAuthorize verifica antes da execução, evitando chamada ao serviço se falhar
    @Test
    @WithMockUser(username = "john")
    void testPreAuthorizeEndpointWithMatchingOwner() throws Exception {
        mockMvc.perform(get("/api/resource/pre/john_resource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Access granted: Resource content for john_resource"));
    }

    // Testa falha no endpoint /api/resource/pre/{id} com usuário não correspondente
    // @PreAuthorize nega acesso antes da execução, sem chamar o serviço
    @Test
    @WithMockUser(username = "other")
        void testPreAuthorizeEndpointAccessDenied() throws Exception {
        mockMvc.perform(get("/api/resource/pre/john_resource"))
                .andExpect(status().isForbidden());
    }
}