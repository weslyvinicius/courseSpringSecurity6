package com.academy.springsecurity6full.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JohnEmployeesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetAllEmployees_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees")
                        .with(httpBasic("john", "j123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Read all employees"));
    }

    @Test
    public void testGetEmployeeById_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/employees/1")
                        .with(httpBasic("john", "j123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Read Employee"));
    }

    @Test
    public void testPostEmployee_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/employees")
                        .with(httpBasic("john", "j123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testPutEmployee_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/employees")
                        .with(httpBasic("john", "j123456")))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testDeleteEmployee_Forbidden() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/employees/1")
                        .with(httpBasic("john", "j123456")))
                .andExpect(status().isForbidden());
    }

}