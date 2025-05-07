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
class MyApiControllerWithMockTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void myFree() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/myfree"))
                .andExpect(status().isOk())
                .andExpect(content().string("Acessando my endpoint free"));
    }

    @Test
    void myAuthenticateUnauthorized() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/authenticate"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "john")
    void myAuthenticateWithJohn() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/authenticate"))
                .andExpect(status().isOk())
                .andExpect(content().string("Acessando my endpoint authenticate"));
    }

    @Test
    @WithMockUser(username = "mary")
    void myAuthenticateWithMary() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/authenticate"))
                .andExpect(status().isOk())
                .andExpect(content().string("Acessando my endpoint authenticate"));
    }

    @Test
    @WithMockUser(username = "susan")
    void myAuthenticateWithSusan() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/authenticate"))
                .andExpect(status().isOk())
                .andExpect(content().string("Acessando my endpoint authenticate"));
    }

}