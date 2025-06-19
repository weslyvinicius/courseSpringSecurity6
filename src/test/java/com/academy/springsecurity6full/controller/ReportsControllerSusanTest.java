package com.academy.springsecurity6full.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ReportsControllerSusanTest extends AbstractJwtTest {


    @Test
    public void testGetAllReports_Authorized() throws Exception {
        mockMvc.perform(get("/api/reports")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Get all reports"));
    }

    @Test
    public void testGetReport_Authorized() throws Exception {
        mockMvc.perform(get("/api/reports/1")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Read Report: 1"));
    }

    @Test
    public void testCreateReport_Authorized() throws Exception {
        mockMvc.perform(post("/api/reports")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Create Report"));
    }

    @Test
    public void testUpdateReport_Authorized() throws Exception {
        mockMvc.perform(put("/api/reports/1")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Update Report: 1"));
    }

    @Test
    public void testDeleteReport_Authorized() throws Exception {
        mockMvc.perform(delete("/api/reports/1")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Delete Report: 1"));
    }

    @Test
    public void testGetCombinedAuth_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/reports/combined-auth")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("susan", "s123456")))
                .andExpect(status().isForbidden());
    }
}
