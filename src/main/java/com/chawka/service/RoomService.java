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
    private final SecureRandom random = new SecureRandom();

    public Room createRoom(String hostName) {
        String code = generateCode();
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

    public void leaveRoom(String code, String memberName) {
        Room room = rooms.get(code);
        if (room == null) {
            return;
        }
        if (room.isHost(memberName)) {
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

    private String generateCode() {
        String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        String code = sb.toString();
        if (rooms.containsKey(code)) {
            return generateCode();
        }
        return code;
    }
}
