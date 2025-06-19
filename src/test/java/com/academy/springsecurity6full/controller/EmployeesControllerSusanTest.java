package com.academy.springsecurity6full.controller;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EmployeesControllerSusanTest extends AbstractJwtTest {


    @Test
    public void testGetAllEmployees_PermitAll() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("john", "j123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Read all employees"));
    }

    @Test
    public void testGetEmployee_Authorized() throws Exception {
        mockMvc.perform(get("/api/employees/1")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Read Employee: 1"));
    }

    @Test
    public void testSaveEmployee_Authorized() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Create Employee"));
    }

    @Test
    public void testUpdateEmployee_Authorized() throws Exception {
        mockMvc.perform(put("/api/employees/1")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Update Employee: 1"));
    }

    @Test
    public void testDeleteEmployee_Authorized() throws Exception {
        mockMvc.perform(delete("/api/employees/1")
                        .header(AUTHORIZATION, "Bearer " + genereteJwtToken("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Delete Employee: 1"));
    }
}