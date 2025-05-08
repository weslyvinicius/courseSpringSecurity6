package com.academy.springsecurity6full.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SusanEmployeesControllerWithMockTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "susan", roles = {"MANAGER","EMPLOYEE","ADMIN"})
    public void testGetAllEmployees_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().string("Read all employees"));
    }

    @Test
    @WithMockUser(username = "susan", roles = {"MANAGER","EMPLOYEE","ADMIN"})
    public void testGetEmployeeById_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Read Employee"));
    }

    @Test
    @WithMockUser(username = "susan", roles = {"MANAGER","EMPLOYEE","ADMIN"})
    public void testPostEmployee_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().string("Create Employee"));
    }

    @Test
    @WithMockUser(username = "susan", roles = {"MANAGER","EMPLOYEE","ADMIN"})
    public void testPutEmployee_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(content().string("Update Employee"));
    }

    @Test
    @WithMockUser(username = "susan", roles = {"MANAGER","EMPLOYEE","ADMIN"})
    public void testDeleteEmployee_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Delete Employee"));
    }

}