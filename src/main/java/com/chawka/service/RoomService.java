package com.chawka.service;

import com.chawka.model.Room;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    public static final long INVITE_TTL_MS = 2 * 60 * 1000L;

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    /** Maps invite codes and open codes to room codes for fast lookup */
    private final Map<String, String> inviteIndex = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public Room createRoom(String hostName) {
        String code = generateCode(8);
        Room room = new Room(code, hostName);
        rooms.put(code, room);
        log.debug("createRoom — code='{}', host='{}'", code, hostName);
        return room;
    }

    public Optional<Room> getRoom(String code) {
        return Optional.ofNullable(rooms.get(code));
    }

    public Optional<Room> joinRoom(String code, String memberName) {
        Room room = rooms.get(code);
        if (room == null) {
            log.debug("joinRoom — room '{}' not found", code);
            return Optional.empty();
        }
        room.addParticipant(memberName);
        log.debug("joinRoom — '{}' joined room '{}'", memberName, code);
        return Optional.of(room);
    }

    public static final class InviteCode {
        private final String code;
        private final long expiresAt;

        public InviteCode(String code, long expiresAt) {
            this.code = code;
            this.expiresAt = expiresAt;
        }

        public String getCode() {
            return code;
        }

        public long getExpiresAt() {
            return expiresAt;
        }
    }

    /** Generate a one-time invite code for a room. Only the host should call this. */
    public InviteCode generateInviteCode(String roomCode) {
        Room room = rooms.get(roomCode);
        if (room == null) {
            return null;
        }
        cleanupExpiredInvites(roomCode, room);

        String invite = "inv-" + generateCode(10);
        long expiresAt = System.currentTimeMillis() + INVITE_TTL_MS;
        room.addInviteCode(invite, expiresAt);
        inviteIndex.put(invite, roomCode);
        log.debug("generateInviteCode — room='{}', invite='{}'", roomCode, invite);
        return new InviteCode(invite, expiresAt);
    }

    /** Generate or return the existing reusable open code for a room. */
    public String getOrCreateOpenCode(String roomCode) {
        Room room = rooms.get(roomCode);
        if (room == null) {
            return null;
        }
        if (room.getOpenCode() != null) {
            return room.getOpenCode();
        }
        String open = "open-" + generateCode(8);
        room.setOpenCode(open);
        inviteIndex.put(open, roomCode);
        return open;
    }

    /** Join a room via invite code (one-time) or open code (reusable). */
    public Optional<Room> joinViaCode(String shareCode, String memberName) {
        String roomCode = inviteIndex.get(shareCode);
        if (roomCode == null) {
            return Optional.empty();
        }
        Room room = rooms.get(roomCode);
        if (room == null) {
            inviteIndex.remove(shareCode);
            return Optional.empty();
        }

        // One-time invite: consume it
        if (shareCode.startsWith("inv-")) {
            if (!room.consumeInviteCode(shareCode)) {
                inviteIndex.remove(shareCode);
                return Optional.empty(); // already used
            }
            inviteIndex.remove(shareCode);
        }

        room.addParticipant(memberName);
        return Optional.of(room);
    }

    public void leaveRoom(String code, String memberName) {
        log.debug("leaveRoom — code='{}', member='{}'", code, memberName);
        Room room = rooms.get(code);
        if (room == null) {
            return;
        }
        if (room.isHost(memberName)) {
            // Clean up all invite/open codes for this room
            room.getInviteCodes().forEach(inviteIndex::remove);
            if (room.getOpenCode() != null) {
                inviteIndex.remove(room.getOpenCode());
            }
            rooms.remove(code);
        } else {
            room.removeParticipant(memberName);
        }
    }

    public void updateRoomState(String code, Map<String, Object> newState) {
        Room room = rooms.get(code);
        if (room != null) {
            room.setSharedState(newState);
        }
    }

    // ── Stats ──

    public int getRoomCount() {
        return rooms.size();
    }

    public int getTotalParticipants() {
        return rooms.values().stream()
                .mapToInt(r -> r.getParticipants().size())
                .sum();
    }

    public Collection<Room> getAllRooms() {
        return rooms.values();
    }

    private String generateCode(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        String code = sb.toString();
        if (rooms.containsKey(code) || inviteIndex.containsKey(code)) {
            return generateCode(length);
        }
        return code;
    }

    private void cleanupExpiredInvites(String roomCode, Room room) {
        inviteIndex.entrySet().removeIf(entry -> roomCode.equals(entry.getValue()) && entry.getKey().startsWith("inv-"));
        room.removeExpiredInviteCodes(System.currentTimeMillis());
        room.getInviteCodes().forEach(code -> inviteIndex.put(code, roomCode));
    }
}
