package com.chawka.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Map;

@Entity
@Table(name = "roqia_shares")
public class RoqiaShare {

    @Id
    private String id;

    private String fileRecordId;
    private String title;
    private String sharedBy;
    private String sharedDate;

    @Column(columnDefinition = "TEXT")
    private String note;

    @Column(columnDefinition = "TEXT")
    private String transcript;

    private long createdAt;
    private int viewCount;

    public RoqiaShare() {}

    public RoqiaShare(Map<String, Object> data) {
        this.id = (String) data.get("id");
        this.fileRecordId = (String) data.get("fileRecordId");
        this.title = (String) data.get("title");
        this.sharedBy = (String) data.get("sharedBy");
        this.sharedDate = (String) data.get("sharedDate");
        this.note = (String) data.getOrDefault("note", "");
        this.transcript = (String) data.getOrDefault("transcript", "");
        this.createdAt = data.containsKey("createdAt") ? ((Number) data.get("createdAt")).longValue() : System.currentTimeMillis();
        this.viewCount = data.containsKey("viewCount") ? ((Number) data.get("viewCount")).intValue() : 0;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getFileRecordId() { return fileRecordId; }
    public void setFileRecordId(String fileRecordId) { this.fileRecordId = fileRecordId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSharedBy() { return sharedBy; }
    public void setSharedBy(String sharedBy) { this.sharedBy = sharedBy; }
    public String getSharedDate() { return sharedDate; }
    public void setSharedDate(String sharedDate) { this.sharedDate = sharedDate; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getTranscript() { return transcript; }
    public void setTranscript(String transcript) { this.transcript = transcript; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public int getViewCount() { return viewCount; }
    public void setViewCount(int viewCount) { this.viewCount = viewCount; }
}
