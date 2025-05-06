package com.academy.springsecurity6full.controller;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerMaryWithMockTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "mary", roles = {"MANAGER"})
    public void testGetAllEmployees_PermitAll() throws Exception {
        mockMvc.perform(get("/api/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Read all admin employees"));
    }

    @Test
    @WithMockUser(username = "mary", roles = {"MANAGER"})
    public void testGetAdminEmployee_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "mary", roles = {"MANAGER"})
    public void testSaveAdminEmployee_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "mary", roles = {"MANAGER"})
    public void testUpdateAdminEmployee_Unauthorized() throws Exception {
        mockMvc.perform(put("/api/admin/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "mary", roles = {"MANAGER"})
    public void testDeleteAdminEmployee_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/admin/1"))
                .andExpect(status().isForbidden());
    }
}
