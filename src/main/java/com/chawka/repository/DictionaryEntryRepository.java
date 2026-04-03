package com.chawka.repository;

import com.chawka.model.DictionaryEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DictionaryEntryRepository extends JpaRepository<DictionaryEntry, String> {
    List<DictionaryEntry> findByWordOrderByReputationDescCreatedAtDesc(String word);
    List<DictionaryEntry> findAllByOrderByReputationDescCreatedAtDesc();
}
