package com.chawka.service;

import com.chawka.model.Room;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RoomService {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    /** Maps invite codes and open codes to room codes for fast lookup */
    private final Map<String, String> inviteIndex = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    public Room createRoom(String hostName) {
        String code = generateCode(8);
        Room room = new Room(code, hostName);
        rooms.put(code, room);
        return room;
    }

    public Optional<Room> getRoom(String code) {
        return Optional.ofNullable(rooms.get(code));
    }

    public Optional<Room> joinRoom(String code, String memberName) {
        Room room = rooms.get(code);
        if (room == null) {
            return Optional.empty();
        }
        room.addParticipant(memberName);
        return Optional.of(room);
    }

    /** Generate a one-time invite code for a room. Only the host should call this. */
    public String generateInviteCode(String roomCode) {
        Room room = rooms.get(roomCode);
        if (room == null) {
            return null;
        }
        String invite = "inv-" + generateCode(10);
        room.addInviteCode(invite);
        inviteIndex.put(invite, roomCode);
        return invite;
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
                return Optional.empty(); // already used
            }
            inviteIndex.remove(shareCode);
        }

        room.addParticipant(memberName);
        return Optional.of(room);
    }

    public void leaveRoom(String code, String memberName) {
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
}
