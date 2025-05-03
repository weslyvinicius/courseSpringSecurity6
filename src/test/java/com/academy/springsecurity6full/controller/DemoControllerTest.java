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

@AutoConfigureMockMvc
@SpringBootTest
class DemoControllerTest {

    @Autowired
    MockMvc mockMvc;

     @Test
     void demoOK() throws Exception {
         mockMvc.perform( MockMvcRequestBuilders.get("/demo")
                                 .with( httpBasic("key", "secret") )
                         )
                 .andExpect(status().isOk())
                 .andExpect(content().string("Demo!"));
     }

    @Test
    void demoBadRequest() throws Exception {
        mockMvc.perform( MockMvcRequestBuilders.get("/demo")
                        .with( httpBasic("key", "secret") )
                       )
                .andExpect(status().isBadRequest());
    }

}