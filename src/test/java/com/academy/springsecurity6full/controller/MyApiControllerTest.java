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
class MyApiControllerTest {

     @Autowired
     private MockMvc mockMvc;

     @Test
     void myFree() throws Exception {

         mockMvc.perform(MockMvcRequestBuilders.get("/myfree") )
                 .andExpect( status().isOk() )
                 .andExpect( content().string( "Acessando my endpoint free" ) );

     }

        @Test
        void myAuthenticate() throws Exception {

            mockMvc.perform(MockMvcRequestBuilders.get("/authenticate"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(MockMvcRequestBuilders.get("/authenticate")
                            .with(httpBasic("batman", "123456")))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Acessando my endpoint authenticate"));
        }

    @Test
    void myAuthenticateUnauthorized() throws Exception {

        mockMvc.perform(MockMvcRequestBuilders.get("/authenticate"))
                .andExpect(status().isUnauthorized());

    }



}