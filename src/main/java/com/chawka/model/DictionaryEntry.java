package com.chawka.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "dictionary_entries")
public class DictionaryEntry {

    @Id
    private String id;

    @Column(nullable = false)
    private String word;

    @Column(columnDefinition = "TEXT")
    private String definition;

    private String author;
    private long createdAt;
    private int reputation;

    public DictionaryEntry() {}

    public DictionaryEntry(String word, String definition, String author) {
        this.id = "dict-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 99999);
        this.word = word;
        this.definition = definition;
        this.author = author == null ? "" : author;
        this.createdAt = System.currentTimeMillis();
        this.reputation = 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }

    public String getDefinition() { return definition; }
    public void setDefinition(String definition) { this.definition = definition; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public int getReputation() { return reputation; }
    public void setReputation(int reputation) { this.reputation = reputation; }
}
