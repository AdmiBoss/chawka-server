package com.chawka.service;

import com.chawka.model.DictionaryEntry;
import com.chawka.repository.DictionaryEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class DictionaryService {

    private static final Logger log = LoggerFactory.getLogger(DictionaryService.class);
    private final DictionaryEntryRepository repo;

    public DictionaryService(DictionaryEntryRepository repo) {
        this.repo = repo;
    }

    public List<DictionaryEntry> getByWord(String word) {
        log.debug("getByWord('{}') — searching entries", word);
        return repo.findByWordOrderByReputationDescCreatedAtDesc(word);
    }

    @Transactional
    public DictionaryEntry add(String word, String definition, String author) {
        DictionaryEntry entry = new DictionaryEntry(word, definition, author);
        entry = repo.save(entry);
        log.debug("Added definition id='{}' for word='{}' by author='{}'", entry.getId(), word, author);
        return entry;
    }

    public Map<String, List<DictionaryEntry>> getAllGrouped() {
        return repo.findAllByOrderByReputationDescCreatedAtDesc().stream()
                .collect(Collectors.groupingBy(DictionaryEntry::getWord, LinkedHashMap::new, Collectors.toList()));
    }

    public List<DictionaryEntry> getAll() {
        return repo.findAllByOrderByReputationDescCreatedAtDesc();
    }

    @Transactional
    public Optional<DictionaryEntry> upvote(String id) {
        return repo.findById(id).map(entry -> {
            entry.setReputation(entry.getReputation() + 1);
            log.debug("upvote('{}') — reputation now {}", id, entry.getReputation());
            return repo.save(entry);
        });
    }
}
