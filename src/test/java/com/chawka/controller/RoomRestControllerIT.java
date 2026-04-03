package com.chawka.controller;

import com.chawka.TestS3Config;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestS3Config.class)
class RoomRestControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createRoom_returns200WithCode() throws Exception {
        mockMvc.perform(post("/api/rooms/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hostName\":\"Ali\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").isNotEmpty())
                .andExpect(jsonPath("$.participants").isArray())
                .andExpect(jsonPath("$.roomState").isMap());
    }

    @Test
    void createRoom_missingHost_returns400() throws Exception {
        mockMvc.perform(post("/api/rooms/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void joinRoom_unknownCode_returns404() throws Exception {
        mockMvc.perform(post("/api/rooms/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"nonexistent\",\"memberName\":\"Hassan\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createAndJoin_fullCycle() throws Exception {
        // Create room
        MvcResult createResult = mockMvc.perform(post("/api/rooms/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hostName\":\"Ali\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String code = com.jayway.jsonpath.JsonPath.read(
                createResult.getResponse().getContentAsString(), "$.code");

        // Join room
        mockMvc.perform(post("/api/rooms/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"memberName\":\"Hassan\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participants.length()").value(2));
    }
}
