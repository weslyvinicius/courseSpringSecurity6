package com.academy.springsecurity6full.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class AdminControllerSusanTest extends AbstractJwtTest {


    @Test
    public void testGetAllEmployees_PermitAll() throws Exception {
        mockMvc.perform(get("/api/admin")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Read all admin employees"));
    }

    @Test
    public void testGetAdminEmployee_Authorized() throws Exception {
        mockMvc.perform(get("/api/admin/1")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Read Admin Employee: 1"));
    }

    @Test
    public void testSaveAdminEmployee_Authorized() throws Exception {
        mockMvc.perform(post("/api/admin")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Create Admin Employee"));
    }

    @Test
    public void testUpdateAdminEmployee_Authorized() throws Exception {
        mockMvc.perform(put("/api/admin/1")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Update Admin Employee: 1"));
    }

    @Test
    public void testDeleteAdminEmployee_Authorized() throws Exception {
        mockMvc.perform(delete("/api/admin/1")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Delete Admin Employee: 1"));
    }
}
