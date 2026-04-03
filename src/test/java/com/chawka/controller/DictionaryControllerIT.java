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
class DictionaryControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getAll_empty_returns200() throws Exception {
        mockMvc.perform(get("/api/dictionary"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void addAndGet_fullCycle() throws Exception {
        // Add a definition
        mockMvc.perform(post("/api/dictionary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"word\":\"salam\",\"definition\":\"peace\",\"author\":\"Ali\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.word").value("salam"))
                .andExpect(jsonPath("$.definition").value("peace"))
                .andExpect(jsonPath("$.author").value("Ali"));

        // Fetch by word
        mockMvc.perform(get("/api/dictionary/salam"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].word").value("salam"));
    }

    @Test
    void addDefinition_missingWord_returns400() throws Exception {
        mockMvc.perform(post("/api/dictionary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"definition\":\"peace\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upvote_nonExistentId_returns404() throws Exception {
        mockMvc.perform(post("/api/dictionary/nonexistent/upvote"))
                .andExpect(status().isNotFound());
    }
}
