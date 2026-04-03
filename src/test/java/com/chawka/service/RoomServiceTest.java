package com.chawka.service;

import com.chawka.model.Room;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoomServiceTest {

    private RoomService roomService;

    @BeforeEach
    void setUp() {
        roomService = new RoomService();
    }

    @Test
    void createRoom_returnsRoomWithHostAsParticipant() {
        Room room = roomService.createRoom("Ali");
        assertNotNull(room.getCode());
        assertEquals("Ali", room.getHostName());
        assertEquals(1, room.getParticipants().size());
        assertEquals("Ali", room.getParticipants().get(0).getName());
        assertEquals("admin", room.getParticipants().get(0).getRole());
    }

    @Test
    void getRoom_existingCode_returnsRoom() {
        Room created = roomService.createRoom("Ali");
        Optional<Room> found = roomService.getRoom(created.getCode());
        assertTrue(found.isPresent());
        assertEquals(created.getCode(), found.get().getCode());
    }

    @Test
    void getRoom_unknownCode_returnsEmpty() {
        assertTrue(roomService.getRoom("nonexistent").isEmpty());
    }

    @Test
    void joinRoom_addsParticipant() {
        Room room = roomService.createRoom("Ali");
        Optional<Room> joined = roomService.joinRoom(room.getCode(), "Hassan");
        assertTrue(joined.isPresent());
        assertEquals(2, joined.get().getParticipants().size());
    }

    @Test
    void joinRoom_duplicateName_doesNotAddTwice() {
        Room room = roomService.createRoom("Ali");
        roomService.joinRoom(room.getCode(), "Hassan");
        roomService.joinRoom(room.getCode(), "Hassan");
        assertEquals(2, roomService.getRoom(room.getCode()).get().getParticipants().size());
    }

    @Test
    void joinRoom_unknownCode_returnsEmpty() {
        assertTrue(roomService.joinRoom("bad", "Hassan").isEmpty());
    }

    @Test
    void leaveRoom_hostLeaving_removesRoom() {
        Room room = roomService.createRoom("Ali");
        roomService.leaveRoom(room.getCode(), "Ali");
        assertTrue(roomService.getRoom(room.getCode()).isEmpty());
    }

    @Test
    void leaveRoom_memberLeaving_keepsRoom() {
        Room room = roomService.createRoom("Ali");
        roomService.joinRoom(room.getCode(), "Hassan");
        roomService.leaveRoom(room.getCode(), "Hassan");
        Optional<Room> r = roomService.getRoom(room.getCode());
        assertTrue(r.isPresent());
        assertEquals(1, r.get().getParticipants().size());
    }

    @Test
    void generateInviteCode_validRoom_returnsCode() {
        Room room = roomService.createRoom("Ali");
        RoomService.InviteCode invite = roomService.generateInviteCode(room.getCode());
        assertNotNull(invite);
        assertTrue(invite.getCode().startsWith("inv-"));
        assertTrue(invite.getExpiresAt() > System.currentTimeMillis());
    }

    @Test
    void generateInviteCode_unknownRoom_returnsNull() {
        assertNull(roomService.generateInviteCode("bad"));
    }

    @Test
    void joinViaCode_inviteCode_consumesOnce() {
        Room room = roomService.createRoom("Ali");
        RoomService.InviteCode invite = roomService.generateInviteCode(room.getCode());

        Optional<Room> joined = roomService.joinViaCode(invite.getCode(), "Hassan");
        assertTrue(joined.isPresent());
        assertEquals(2, joined.get().getParticipants().size());

        // Second use of same invite should fail
        assertTrue(roomService.joinViaCode(invite.getCode(), "Omar").isEmpty());
    }

    @Test
    void getOrCreateOpenCode_returnsReusableCode() {
        Room room = roomService.createRoom("Ali");
        String open1 = roomService.getOrCreateOpenCode(room.getCode());
        String open2 = roomService.getOrCreateOpenCode(room.getCode());
        assertNotNull(open1);
        assertEquals(open1, open2); // same code returned
        assertTrue(open1.startsWith("open-"));
    }

    @Test
    void joinViaCode_openCode_allowsMultipleJoins() {
        Room room = roomService.createRoom("Ali");
        String openCode = roomService.getOrCreateOpenCode(room.getCode());

        assertTrue(roomService.joinViaCode(openCode, "Hassan").isPresent());
        assertTrue(roomService.joinViaCode(openCode, "Omar").isPresent());
        assertEquals(3, roomService.getRoom(room.getCode()).get().getParticipants().size());
    }

    @Test
    void getRoomCount_reflectsCreatedRooms() {
        assertEquals(0, roomService.getRoomCount());
        roomService.createRoom("Ali");
        roomService.createRoom("Hassan");
        assertEquals(2, roomService.getRoomCount());
    }

    @Test
    void getTotalParticipants_sumsAcrossRooms() {
        Room r1 = roomService.createRoom("Ali");
        roomService.joinRoom(r1.getCode(), "Hassan");
        roomService.createRoom("Omar");
        assertEquals(3, roomService.getTotalParticipants());
    }

    @Test
    void getAllRooms_returnsAllRooms() {
        roomService.createRoom("Ali");
        roomService.createRoom("Hassan");
        assertEquals(2, roomService.getAllRooms().size());
    }

    @Test
    void updateRoomState_updatesState() {
        Room room = roomService.createRoom("Ali");
        roomService.updateRoomState(room.getCode(), java.util.Map.of("test", "value"));
        assertEquals("value", roomService.getRoom(room.getCode()).get().getSharedState().get("test"));
    }
}
