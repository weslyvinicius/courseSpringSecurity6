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
public class EmployeesControllerMaryWithMockTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "mary", roles = {"MANAGER","EMPLOYEE"})
    public void testGetAllEmployees_PermitAll() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().string("Read all employees"));
    }

    @Test
    @WithMockUser(username = "mary", roles = {"MANAGER","EMPLOYEE"})
    public void testGetEmployee_Authorized() throws Exception {
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Read Employee"));
    }

    @Test
    @WithMockUser(username = "mary", roles = {"MANAGER","EMPLOYEE"})
    public void testSaveEmployee_Authorized() throws Exception {
        mockMvc.perform(post("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().string("Create Employee"));
    }

    @Test
    @WithMockUser(username = "mary", roles = {"MANAGER","EMPLOYEE"})
    public void testUpdateEmployee_Authorized() throws Exception {
        mockMvc.perform(put("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().string("Update Employee"));
    }

    @Test
    @WithMockUser(username = "mary", roles = {"MANAGER","EMPLOYEE"})
    public void testDeleteEmployee_Unauthorized() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }
}