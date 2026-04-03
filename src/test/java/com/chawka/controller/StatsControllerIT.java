package com.chawka.controller;

import com.chawka.TestS3Config;
import com.chawka.service.RoomService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestS3Config.class)
class StatsControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RoomService roomService;

    @Test
    void stats_returnsAllFields() throws Exception {
        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.roomCount").isNumber())
                .andExpect(jsonPath("$.totalParticipants").isNumber())
                .andExpect(jsonPath("$.roomsByType").isMap())
                .andExpect(jsonPath("$.dictionaryWords").isNumber())
                .andExpect(jsonPath("$.dictionaryEntries").isNumber())
                .andExpect(jsonPath("$.khotabCount").isNumber())
                .andExpect(jsonPath("$.roqiaCount").isNumber())
                .andExpect(jsonPath("$.filesCount").isNumber());
    }

    @Test
    void stats_reflectsRoomCreation() throws Exception {
        roomService.createRoom("TestHost");

        mockMvc.perform(get("/api/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomCount").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.totalParticipants").value(org.hamcrest.Matchers.greaterThanOrEqualTo(1)));
    }
}
