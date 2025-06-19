package com.academy.springsecurity6full.controller;


import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerMaryTest extends AbstractJwtTest {


    @Test
    public void testGetAllEmployees_PermitAll() throws Exception {
        mockMvc.perform(get("/api/admin")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("mary", "m123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Read all admin employees"));
    }

    @Test
    public void testGetAdminEmployee_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/admin/1")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("mary", "m123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testSaveAdminEmployee_Unauthorized() throws Exception {
        mockMvc.perform(post("/api/admin")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("mary", "m123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testUpdateAdminEmployee_Unauthorized() throws Exception {
        mockMvc.perform(put("/api/admin/1")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("mary", "m123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteAdminEmployee_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/admin/1")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("mary", "m123456")))
                .andExpect(status().isForbidden());
    }
}
