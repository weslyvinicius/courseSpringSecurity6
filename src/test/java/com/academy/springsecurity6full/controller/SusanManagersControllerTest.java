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
class SusanManagersControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetAllEmployees_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin")
                        .with(httpBasic("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Read all admin employees"));
    }

    @Test
    public void testGetEmployeeById_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/admin/1")
                        .with(httpBasic("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Read Admin Employee"));
    }

    @Test
    public void testPostEmployee_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/admin")
                        .with(httpBasic("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Create Admin Employee"));
    }

    @Test
    public void testPutEmployee_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.put("/api/admin")
                        .with(httpBasic("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Update Admin Employee"));
    }

    @Test
    public void testDeleteEmployee_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/admin/1")
                        .with(httpBasic("susan", "s123456")))
                .andExpect(status().isOk())
                .andExpect(content().string("Delete Admin Employee"));
    }

}