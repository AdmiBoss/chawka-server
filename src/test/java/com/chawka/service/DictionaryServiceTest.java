package com.chawka.service;

import com.chawka.model.DictionaryEntry;
import com.chawka.repository.DictionaryEntryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(DictionaryService.class)
class DictionaryServiceTest {

    @Autowired
    private DictionaryService dictionaryService;

    @Autowired
    private DictionaryEntryRepository repo;

    @Test
    void add_persistsEntry() {
        DictionaryEntry entry = dictionaryService.add("salam", "peace", "Ali");
        assertNotNull(entry.getId());
        assertEquals("salam", entry.getWord());
        assertEquals("peace", entry.getDefinition());
        assertEquals("Ali", entry.getAuthor());
        assertEquals(0, entry.getReputation());
    }

    @Test
    void getByWord_returnsMatchingEntries() {
        dictionaryService.add("salam", "peace", "Ali");
        dictionaryService.add("salam", "greeting", "Hassan");
        dictionaryService.add("quran", "holy book", "Omar");

        List<DictionaryEntry> results = dictionaryService.getByWord("salam");
        assertEquals(2, results.size());
    }

    @Test
    void getAllGrouped_groupsByWord() {
        dictionaryService.add("salam", "peace", "Ali");
        dictionaryService.add("salam", "greeting", "Hassan");
        dictionaryService.add("quran", "holy book", "Omar");

        Map<String, List<DictionaryEntry>> grouped = dictionaryService.getAllGrouped();
        assertEquals(2, grouped.size());
        assertTrue(grouped.containsKey("salam"));
        assertTrue(grouped.containsKey("quran"));
        assertEquals(2, grouped.get("salam").size());
    }

    @Test
    void upvote_incrementsReputation() {
        DictionaryEntry entry = dictionaryService.add("salam", "peace", "Ali");
        Optional<DictionaryEntry> updated = dictionaryService.upvote(entry.getId());
        assertTrue(updated.isPresent());
        assertEquals(1, updated.get().getReputation());
    }

    @Test
    void upvote_nonExistentId_returnsEmpty() {
        assertTrue(dictionaryService.upvote("nonexistent").isEmpty());
    }

    @Test
    void getAll_orderedByReputation() {
        DictionaryEntry e1 = dictionaryService.add("word1", "def1", "a");
        DictionaryEntry e2 = dictionaryService.add("word2", "def2", "b");
        dictionaryService.upvote(e2.getId());
        dictionaryService.upvote(e2.getId());

        List<DictionaryEntry> all = dictionaryService.getAll();
        assertEquals(2, all.size());
        assertEquals(e2.getId(), all.get(0).getId());
    }
}
