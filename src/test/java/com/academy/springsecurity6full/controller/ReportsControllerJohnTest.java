package com.academy.springsecurity6full.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ReportsControllerJohnTest extends AbstractJwtTest {


    @Test
    public void testGetAllReports_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/reports")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("john", "j123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetReport_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/reports/1")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("john", "j123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testCreateReport_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/reports")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("john", "j123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUpdateReport_Unauthorized() throws Exception {
        mockMvc.perform(put("/api/reports/1")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("john", "j123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteReport_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/reports/1")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("john", "j123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetCombinedAuth_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/reports/combined-auth")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("john", "j123456")))
                .andExpect(status().isForbidden());
    }
}