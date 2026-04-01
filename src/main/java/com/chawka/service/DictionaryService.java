package com.chawka.service;

import com.chawka.model.DictionaryEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class DictionaryService {

    private static final Logger log = LoggerFactory.getLogger(DictionaryService.class);
    private final Map<String, DictionaryEntry> entriesById = new ConcurrentHashMap<>();

    public List<DictionaryEntry> getByWord(String word) {
        log.debug("getByWord('{}') — searching entries", word);
        return entriesById.values().stream()
                .filter(e -> word.equals(e.getWord()))
                .sorted(Comparator.comparingInt(DictionaryEntry::getReputation).reversed()
                        .thenComparingLong(DictionaryEntry::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public DictionaryEntry add(String word, String definition, String author) {
        DictionaryEntry entry = new DictionaryEntry(word, definition, author);
        entriesById.put(entry.getId(), entry);
        log.debug("Added definition id='{}' for word='{}' by author='{}'", entry.getId(), word, author);
        return entry;
    }

    /** All entries grouped by word, each group sorted by reputation desc */
    public Map<String, List<DictionaryEntry>> getAllGrouped() {
        return entriesById.values().stream()
                .sorted(Comparator.comparingInt(DictionaryEntry::getReputation).reversed()
                        .thenComparingLong(DictionaryEntry::getCreatedAt).reversed())
                .collect(Collectors.groupingBy(DictionaryEntry::getWord, LinkedHashMap::new, Collectors.toList()));
    }

    /** Flat list of all entries sorted by reputation desc */
    public List<DictionaryEntry> getAll() {
        return entriesById.values().stream()
                .sorted(Comparator.comparingInt(DictionaryEntry::getReputation).reversed()
                        .thenComparingLong(DictionaryEntry::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public Optional<DictionaryEntry> upvote(String id) {
        DictionaryEntry entry = entriesById.get(id);
        if (entry == null) {
            log.debug("upvote('{}') — entry not found", id);
            return Optional.empty();
        }
        synchronized (entry) {
            entry.setReputation(entry.getReputation() + 1);
        }
        log.debug("upvote('{}') — reputation now {}", id, entry.getReputation());
        return Optional.of(entry);
    }
}
