package com.academy.springsecurity6full.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerSusanTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetAllEmployees_PermitAll() throws Exception {
        mockMvc.perform(get("/api/admin")
                        .with(httpBasic("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Read all admin employees"));
    }

    @Test
    public void testGetAdminEmployee_Authorized() throws Exception {
        mockMvc.perform(get("/api/admin/1")
                        .with(httpBasic("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Read Admin Employee: 1"));
    }

    @Test
    public void testSaveAdminEmployee_Authorized() throws Exception {
        mockMvc.perform(post("/api/admin")
                        .with(httpBasic("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Create Admin Employee"));
    }

    @Test
    public void testUpdateAdminEmployee_Authorized() throws Exception {
        mockMvc.perform(put("/api/admin/1")
                .with(httpBasic("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Update Admin Employee: 1"));
    }

    @Test
    public void testDeleteAdminEmployee_Authorized() throws Exception {
        mockMvc.perform(delete("/api/admin/1")
                        .with(httpBasic("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Delete Admin Employee: 1"));
    }
}
