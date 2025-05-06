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
public class ReportsControllerSusanwithMockTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "susan", authorities = {"ROLE_ADMIN", "READ_EMPLOYEE", "CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "DELETE_EMPLOYEE", "READ_REPORT", "CREATE_REPORT", "UPDATE_REPORT", "DELETE_REPORT"})
    public void testGetAllReports_Authorized() throws Exception {
        mockMvc.perform(get("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(content().string("Get all reports"));
    }

    @Test
    @WithMockUser(username = "susan", authorities = {"ROLE_ADMIN", "READ_EMPLOYEE", "CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "DELETE_EMPLOYEE", "READ_REPORT", "CREATE_REPORT", "UPDATE_REPORT", "DELETE_REPORT"})
    public void testGetReport_Authorized() throws Exception {
        mockMvc.perform(get("/api/reports/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Read Report: 1"));
    }

    @Test
    @WithMockUser(username = "susan", authorities = {"ROLE_ADMIN", "READ_EMPLOYEE", "CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "DELETE_EMPLOYEE", "READ_REPORT", "CREATE_REPORT", "UPDATE_REPORT", "DELETE_REPORT"})
    public void testCreateReport_Authorized() throws Exception {
        mockMvc.perform(post("/api/reports"))
                .andExpect(status().isOk())
                .andExpect(content().string("Create Report"));
    }

    @Test
    @WithMockUser(username = "susan", authorities = {"ROLE_ADMIN", "READ_EMPLOYEE", "CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "DELETE_EMPLOYEE", "READ_REPORT", "CREATE_REPORT", "UPDATE_REPORT", "DELETE_REPORT"})
    public void testUpdateReport_Authorized() throws Exception {
        mockMvc.perform(put("/api/reports/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Update Report: 1"));
    }

    @Test
    @WithMockUser(username = "susan", authorities = {"ROLE_ADMIN", "READ_EMPLOYEE", "CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "DELETE_EMPLOYEE", "READ_REPORT", "CREATE_REPORT", "UPDATE_REPORT", "DELETE_REPORT"})
    public void testDeleteReport_Authorized() throws Exception {
        mockMvc.perform(delete("/api/reports/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Delete Report: 1"));
    }

    @Test
    @WithMockUser(username = "susan", authorities = {"ROLE_ADMIN", "READ_EMPLOYEE", "CREATE_EMPLOYEE", "UPDATE_EMPLOYEE", "DELETE_EMPLOYEE", "READ_REPORT", "CREATE_REPORT", "UPDATE_REPORT", "DELETE_REPORT"})
    public void testGetCombinedAuth_Unauthorized() throws Exception {
        mockMvc.perform(get("/api/reports/combined-auth"))
                .andExpect(status().isForbidden());
    }
}
