package com.chawka.controller;

import com.chawka.model.DictionaryEntry;
import com.chawka.service.DictionaryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dictionary")
public class DictionaryController {

    private static final Logger log = LoggerFactory.getLogger(DictionaryController.class);
    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    /** GET /api/dictionary — all entries grouped by word */
    @GetMapping
    public java.util.Map<String, List<DictionaryEntry>> getAll() {
        log.debug("GET /api/dictionary — fetching all grouped entries");
        Map<String, List<DictionaryEntry>> result = dictionaryService.getAllGrouped();
        log.debug("Returning {} word groups", result.size());
        return result;
    }

    /** GET /api/dictionary/{word} — list definitions sorted by reputation desc */
    @GetMapping("/{word}")
    public List<DictionaryEntry> getByWord(@PathVariable String word) {
        log.debug("GET /api/dictionary/{} — fetching definitions", word);
        return dictionaryService.getByWord(word);
    }

    /** POST /api/dictionary — body: { word, definition, author } */
    @PostMapping
    public ResponseEntity<DictionaryEntry> addDefinition(@RequestBody Map<String, String> body) {
        String word = body.get("word");
        String definition = body.get("definition");
        String author = body.getOrDefault("author", "");
        log.debug("POST /api/dictionary — word='{}', author='{}'", word, author);

        if (word == null || word.isBlank() || definition == null || definition.isBlank()) {
            log.warn("Bad request: word or definition is blank");
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(dictionaryService.add(word.trim(), definition.trim(), author.trim()));
    }

    /** POST /api/dictionary/{id}/upvote — increment reputation */
    @PostMapping("/{id}/upvote")
    public ResponseEntity<DictionaryEntry> upvote(@PathVariable String id) {
        log.debug("POST /api/dictionary/{}/upvote", id);
        return dictionaryService.upvote(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
