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
public class EmployeesControllerMaryTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetAllEmployees_PermitAll() throws Exception {
        mockMvc.perform(get("/api/employees")
                        .with(httpBasic("mary", "m123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Read all employees"));
    }

    @Test
    public void testGetEmployee_Authorized() throws Exception {
        mockMvc.perform(get("/api/employees/1")
                        .with(httpBasic("mary", "m123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Read Employee: 1"));
    }

    @Test
    public void testSaveEmployee_Authorized() throws Exception {
        mockMvc.perform(post("/api/employees")
                        .with(httpBasic("mary", "m123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Create Employee"));
    }

    @Test
    public void testUpdateEmployee_Authorized() throws Exception {
        mockMvc.perform(put("/api/employees/1")
                        .with(httpBasic("mary", "m123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Update Employee: 1"));
    }

    @Test
    public void testDeleteEmployee_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/employees/1")
                        .with(httpBasic("mary", "m123456")))
                .andExpect(status().isForbidden());
    }
}
