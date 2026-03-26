package com.chawka.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
    private final String code;
    private final String hostName;
    private final long createdAt;
    private final List<Participant> participants = new ArrayList<>();
    private Map<String, Object> sharedState;

    /** One-time invite codes: code -> true (consumed codes are removed) */
    private final Set<String> inviteCodes = ConcurrentHashMap.newKeySet();

    /** Reusable open code (null = not generated yet) */
    private volatile String openCode;

    public Room(String code, String hostName) {
        this.code = code;
        this.hostName = hostName;
        this.createdAt = System.currentTimeMillis();
        this.participants.add(new Participant(hostName, "admin", this.createdAt));
        this.sharedState = defaultState();
    }

    public String getCode() { return code; }
    public String getHostName() { return hostName; }
    public long getCreatedAt() { return createdAt; }
    public List<Participant> getParticipants() { return participants; }
    public Map<String, Object> getSharedState() { return sharedState; }
    public void setSharedState(Map<String, Object> sharedState) { this.sharedState = sharedState; }

    public String getOpenCode() { return openCode; }
    public void setOpenCode(String openCode) { this.openCode = openCode; }

    public Set<String> getInviteCodes() { return inviteCodes; }

    public void addInviteCode(String inviteCode) {
        inviteCodes.add(inviteCode);
    }

    /** Consume a one-time invite code. Returns true if it was valid. */
    public boolean consumeInviteCode(String inviteCode) {
        return inviteCodes.remove(inviteCode);
    }

    public boolean isHost(String name) {
        return hostName.equals(name);
    }

    public void addParticipant(String name) {
        if (participants.stream().noneMatch(p -> p.getName().equals(name))) {
            participants.add(new Participant(name, "member", System.currentTimeMillis()));
        }
    }

    public void removeParticipant(String name) {
        participants.removeIf(p -> p.getName().equals(name) && !"admin".equals(p.getRole()));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> defaultState() {
        Map<String, Object> state = new HashMap<>();
        Map<String, Object> settings = new HashMap<>();
        settings.put("groupType", "hifdh");
        settings.put("financeMode", "daaret");
        state.put("settings", settings);
        state.put("daaretSessions", new ArrayList<>());
        state.put("hifdhProgress", new ArrayList<>());
        state.put("hifdhSubmissions", new ArrayList<>());
        state.put("taqsimPlan", null);
        return state;
    }

    public static class Participant {
        private final String name;
        private final String role;
        private final long joinedAt;

        public Participant(String name, String role, long joinedAt) {
            this.name = name;
            this.role = role;
            this.joinedAt = joinedAt;
        }

        public String getName() { return name; }
        public String getRole() { return role; }
        public long getJoinedAt() { return joinedAt; }
    }
}
