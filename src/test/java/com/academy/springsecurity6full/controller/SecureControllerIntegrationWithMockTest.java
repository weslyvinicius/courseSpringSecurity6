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
class SecureControllerIntegrationWithMockTest {

    @Autowired
    private MockMvc mockMvc;

    // Testa o endpoint /api/admin com usuário que possui ROLE_ADMIN
    // @Secured("ROLE_ADMIN") verifica se o usuário tem o papel especificado
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminEndpointWithRoleAdmin() throws Exception {
        mockMvc.perform(get("/api/admin"))
                .andExpect(status().isOk()) // Verifica se o status HTTP é 200 (OK)
                .andExpect(content().string("Admin access granted")); // Verifica o corpo da resposta
    }

    // Testa falha no endpoint /api/admin com usuário sem ROLE_ADMIN
    // @Secured nega acesso, retornando status 403 (Forbidden)
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    void testAdminEndpointAccessDenied() throws Exception {
        mockMvc.perform(get("/api/admin"))
                .andExpect(status().isForbidden()); // Verifica se o acesso é negado
    }

    // Testa o endpoint /api/manager-or-admin com usuário que possui ROLE_MANAGER
    // @Secured({"ROLE_ADMIN", "ROLE_MANAGER"}) permite acesso para qualquer um dos papéis listados
    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void testManagerOrAdminEndpointWithRoleManager() throws Exception {
        mockMvc.perform(get("/api/manager-or-admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Manager or Admin access granted"));
    }

    // Testa o endpoint /api/read com usuário que possui a autoridade READ_RESOURCE
    // @Secured("READ_RESOURCE") verifica autoridades granulares
    @Test
    @WithMockUser(username = "john", authorities = {"READ_RESOURCE"})
    void testReadEndpointWithReadAuthority() throws Exception {
        mockMvc.perform(get("/api/read"))
                .andExpect(status().isOk())
                .andExpect(content().string("Read access granted"));
    }

    // Testa o endpoint /api/read-or-write com usuário que possui WRITE_RESOURCE
    // @Secured({"READ_RESOURCE", "WRITE_RESOURCE"}) permite acesso para qualquer uma das autoridades listadas
    @Test
    @WithMockUser(username = "john", authorities = {"WRITE_RESOURCE"})
    void testReadOrWriteEndpointWithWriteAuthority() throws Exception {
        mockMvc.perform(get("/api/read-or-write"))
                .andExpect(status().isOk())
                .andExpect(content().string("Read or Write access granted"));
    }

    // Testa o endpoint /api/resource/{resourceId} com @PreAuthorize e SpEL personalizado
    // @PreAuthorize usa lógica personalizada via SecurityService para verificar se o usuário é o dono do recurso
    // Diferença: @Secured não poderia implementar essa lógica dinâmica
    @Test
    @WithMockUser(username = "john")
    void testCustomResourceEndpointWithMatchingOwner() throws Exception {
        mockMvc.perform(get("/api/resource/john_resource"))
                .andExpect(status().isOk())
                .andExpect(content().string("Custom resource access for john_resource"));
    }

    // Testa falha no endpoint /api/resource/{resourceId} com usuário não correspondente
    // @PreAuthorize nega acesso se o usuário não for o dono do recurso
    @Test
    @WithMockUser(username = "john")
    void testCustomResourceEndpointAccessDenied() throws Exception {
        mockMvc.perform(get("/api/resource/other_resource"))
                .andExpect(status().isForbidden());
    }
}