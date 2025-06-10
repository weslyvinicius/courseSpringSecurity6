package com.academy.springsecurity6full.controller;

import com.academy.springsecurity6full.service.Resource;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecureControllerIntegrationWithMockTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;


    // Testa o endpoint /api/admin com usuário ROLE_ADMIN
    // @Secured verifica antes da execução
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testAdminEndpointWithRoleAdmin() throws Exception {
        mockMvc.perform(get("/api/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Admin access: Content for admin_resource"));
    }

    // Testa falha no endpoint /api/admin com usuário sem ROLE_ADMIN
    // @Secured nega acesso antes da execução
    @Test
    @WithMockUser(username = "john", roles = {"USER"})
    void testAdminEndpointAccessDenied() throws Exception {
        mockMvc.perform(get("/api/admin"))
                .andExpect(status().isForbidden());
    }

    // Testa o endpoint /api/resources/filter com usuário correspondente
    // @PreFilter remove IDs não pertencentes ao usuário antes de chamar o serviço
    @Test
    @WithMockUser(username = "john")
    void testFilterResourcesEndpointWithMatchingUser() throws Exception {
        List<String> resourceIds = new ArrayList<>(List.of("john_resource1", "other_resource2", "john_resource3")); // Lista modificável
        mockMvc.perform(post("/api/resources/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resourceIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // Apenas john_resource1 e john_resource3
                .andExpect(jsonPath("$[0].content").value("Content for john_resource1"))
                .andExpect(jsonPath("$[1].content").value("Content for john_resource3"));
    }

    // Testa o endpoint /api/resources/filter com todos os IDs não correspondentes
    // @PreFilter remove todos os IDs, resultando em uma lista vazia
    @Test
    @WithMockUser(username = "john")
    void testFilterResourcesEndpointWithNoMatchingIds() throws Exception {
        List<String> resourceIds = new ArrayList<>(List.of("other_resource1", "other_resource2")); // Lista modificável
        mockMvc.perform(post("/api/resources/filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resourceIds)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0)); // Lista vazia
    }

    // Testa o endpoint /api/resources/custom-filter com recursos correspondentes
    // @PreFilter remove recursos não pertencentes ao usuário antes da execução
    @Test
    @WithMockUser(username = "john")
    void testCustomFilterResourcesEndpointWithMatchingUser() throws Exception {
        List<Resource> resources = new ArrayList<>(List.of(
                new Resource("john", "Content for john_resource1"),
                new Resource("other", "Content for other_resource2"),
                new Resource("john", "Content for john_resource3")
        )); // Lista modificável
        mockMvc.perform(post("/api/resources/custom-filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resources)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // Apenas recursos de john
                .andExpect(jsonPath("$[0].content").value("Content for john_resource1"))
                .andExpect(jsonPath("$[1].content").value("Content for john_resource3"));
    }

    // Testa o endpoint /api/resources/custom-filter com todos os recursos não correspondentes
    // @PreFilter remove todos os recursos, resultando em uma lista vazia
    @Test
    @WithMockUser(username = "john")
    void testCustomFilterResourcesEndpointWithNoMatchingResources() throws Exception {
        List<Resource> resources = new ArrayList<>(List.of(
                new Resource("other", "Content for other_resource1"),
                new Resource("other", "Content for other_resource2")
        )); // Lista modificável
        mockMvc.perform(post("/api/resources/custom-filter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resources)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0)); // Lista vazia
    }

    // Testa o endpoint /api/resource/pre/{resourceId} com usuário correspondente
    // @PreAuthorize verifica antes da execução
    @Test
    @WithMockUser(username = "john")
    void testPreAuthorizeEndpointWithMatchingOwner() throws Exception {
        mockMvc.perform(get("/api/resource/pre/john_resource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Access granted: Content for john_resource"));
    }

    // Testa falha no endpoint /api/resource/pre/{resourceId} com usuário não correspondente
    // @PreAuthorize nega acesso antes da execução
    @Test
    @WithMockUser(username = "other")
    void testPreAuthorizeEndpointAccessDenied() throws Exception {
        mockMvc.perform(get("/api/resource/pre/john_resource"))
                .andExpect(status().isForbidden());
    }

    // Testa o endpoint /api/resource/check/{resourceId} com usuário com READ_RESOURCE
    // @PreAuthorize verifica antes da execução
    @Test
    @WithMockUser(username = "john", authorities = {"READ_RESOURCE"})
    void testCheckResourceEndpointWithAuthority() throws Exception {
        mockMvc.perform(get("/api/resource/check/john_resource"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value("Read access: Content for john_resource"));
    }

    // Testa falha no endpoint /api/resource/check/{resourceId} com usuário sem READ_RESOURCE
    // @PreAuthorize nega acesso antes da execução
    @Test
    @WithMockUser(username = "other")
    void testCheckResourceEndpointAccessDenied() throws Exception {
        mockMvc.perform(get("/api/resource/check/john_resource"))
                .andExpect(status().isForbidden());
    }

    // Testa o endpoint /api/resources/post-filter com usuário correspondente
    // @PostFilter remove recursos não pertencentes ao usuário após a execução
    @Test
    @WithMockUser(username = "john")
    void testPostFilterResourcesEndpointWithMatchingUser() throws Exception {
        mockMvc.perform(get("/api/resources/post-filter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2)) // Apenas john_resource1 e john_resource3
                .andExpect(jsonPath("$[0].content").value("Content for john_resource1"))
                .andExpect(jsonPath("$[1].content").value("Content for john_resource3"));
    }

    // Testa o endpoint /api/resources/post-filter com usuário correspondente
    // @PostFilter remove recursos não pertencentes ao usuário, mantendo apenas other_resource2
    @Test
    @WithMockUser(username = "other")
    void testPostFilterResourcesEndpointWithNoMatchingResources() throws Exception {
        mockMvc.perform(get("/api/resources/post-filter"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1)) // Apenas other_resource2
                .andExpect(jsonPath("$[0].owner").value("other"))
                .andExpect(jsonPath("$[0].content").value("Content for other_resource2"));
    }
}