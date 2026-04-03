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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestS3Config.class)
class RoqiaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAll_empty_returns200() throws Exception {
        mockMvc.perform(get("/api/roqia"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void createAndGetOne() throws Exception {
        mockMvc.perform(post("/api/roqia")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Morning Roqia\",\"sharedBy\":\"Ali\",\"sharedDate\":\"2026-04-01\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.title").value("Morning Roqia"));
    }

    @Test
    void getOne_nonExistent_returns404() throws Exception {
        mockMvc.perform(get("/api/roqia/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_nonExistent_returns404() throws Exception {
        mockMvc.perform(delete("/api/roqia/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void incrementViews_nonExistent_returns404() throws Exception {
        mockMvc.perform(post("/api/roqia/nonexistent/view"))
                .andExpect(status().isNotFound());
    }
}
