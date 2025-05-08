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
class MaryManagersControllerWithMockTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "mary", roles = {"MANAGER","EMPLOYEE"})
    public void testGetAllEmployees_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Read all admin employees"));
    }

    @Test
    @WithMockUser(username = "mary", roles = {"MANAGER","EMPLOYEE"})
    public void testGetEmployeeById_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "mary", roles = {"MANAGER","EMPLOYEE"})
    public void testPostEmployee_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "mary", roles = {"MANAGER","EMPLOYEE"})
    public void testPutEmployee_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "mary", roles = {"MANAGER","EMPLOYEE"})
    public void testDeleteEmployee_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/1"))
                .andExpect(status().isForbidden());
    }

}