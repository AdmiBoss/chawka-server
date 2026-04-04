package com.chawka.model;

import jakarta.persistence.*;

@Entity
@Table(name = "rooms")
public class RoomRecord {

    @Id
    private String code;

    @Column(nullable = false)
    private String hostName;

    @Column
    private String hostPhone;

    @Column
    private String hostPinHash;

    @Column(columnDefinition = "TEXT")
    private String sharedStateJson;

    @Column(nullable = false)
    private long createdAt;

    @Column(nullable = false)
    private long lastActive;

    @Column(nullable = false)
    private boolean roomActive;

    public RoomRecord() {}

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getHostName() { return hostName; }
    public void setHostName(String hostName) { this.hostName = hostName; }

    public String getHostPhone() { return hostPhone; }
    public void setHostPhone(String hostPhone) { this.hostPhone = hostPhone; }

    public String getHostPinHash() { return hostPinHash; }
    public void setHostPinHash(String hostPinHash) { this.hostPinHash = hostPinHash; }

    public String getSharedStateJson() { return sharedStateJson; }
    public void setSharedStateJson(String sharedStateJson) { this.sharedStateJson = sharedStateJson; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getLastActive() { return lastActive; }
    public void setLastActive(long lastActive) { this.lastActive = lastActive; }

    public boolean isRoomActive() { return roomActive; }
    public void setRoomActive(boolean roomActive) { this.roomActive = roomActive; }
}
