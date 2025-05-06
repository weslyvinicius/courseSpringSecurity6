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
public class ReportsControllerJohnTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "john", authorities = {"ROLE_EMPLOYEE", "READ_EMPLOYEE"})
    public void testGetAllReports_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/reports")
                        .with(httpBasic("john", "j123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "john", authorities = {"ROLE_EMPLOYEE", "READ_EMPLOYEE"})
    public void testGetReport_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/reports/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "john", authorities = {"ROLE_EMPLOYEE", "READ_EMPLOYEE"})
    public void testCreateReport_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/reports"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "john", authorities = {"ROLE_EMPLOYEE", "READ_EMPLOYEE"})
    public void testUpdateReport_Unauthorized() throws Exception {
        mockMvc.perform(put("/api/reports/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "john", authorities = {"ROLE_EMPLOYEE", "READ_EMPLOYEE"})
    public void testDeleteReport_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/reports/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "john", authorities = {"ROLE_EMPLOYEE", "READ_EMPLOYEE"})
    public void testGetCombinedAuth_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/reports/combined-auth"))
                .andExpect(status().isForbidden());
    }
}