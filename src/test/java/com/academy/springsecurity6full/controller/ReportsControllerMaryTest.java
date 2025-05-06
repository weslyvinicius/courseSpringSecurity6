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
public class ReportsControllerMaryTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetAllReports_Authorized() throws Exception {
        mockMvc.perform(get("/api/reports")
                        .with(httpBasic("mary", "m123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Get all reports"));
    }

    @Test
    public void testGetReport_Authorized() throws Exception {
        mockMvc.perform(get("/api/reports/1")
                        .with(httpBasic("mary", "m123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Read Report: 1"));
    }

    @Test
    public void testCreateReport_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/reports")
                        .with(httpBasic("mary", "m123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUpdateReport_Unauthorized() throws Exception {
        mockMvc.perform(put("/api/reports/1")
                        .with(httpBasic("mary", "m123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteReport_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/reports/1")
                        .with(httpBasic("mary", "m123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetCombinedAuth_Authorized() throws Exception {
        mockMvc.perform(get("/api/reports/combined-auth")
                        .with(httpBasic("mary", "m123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("This endpoint requires both READ_REPORT authority and MANAGER role"));
    }
}
