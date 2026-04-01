package com.chawka.controller;

import com.chawka.model.Room;
import com.chawka.service.RoomService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomRestController {

    private static final Logger log = LoggerFactory.getLogger(RoomRestController.class);
    private final RoomService roomService;

    public RoomRestController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createRoom(@RequestBody Map<String, String> body) {
        String hostName = body.get("hostName");
        log.debug("POST /api/rooms/create — hostName='{}'", hostName);
        if (hostName == null || hostName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "hostName required"));
        }
        Room room = roomService.createRoom(hostName.trim());
        log.debug("Room created: code='{}'", room.getCode());
        return ResponseEntity.ok(Map.of(
                "code", room.getCode(),
                "participants", room.getParticipants(),
                "roomState", room.getSharedState()
        ));
    }

    @PostMapping("/join")
    public ResponseEntity<Map<String, Object>> joinRoom(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String memberName = body.get("memberName");
        log.debug("POST /api/rooms/join — code='{}', member='{}'", code, memberName);
        if (code == null || code.isBlank() || memberName == null || memberName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "code and memberName required"));
        }
        return roomService.joinRoom(code.trim(), memberName.trim())
                .map(room -> ResponseEntity.ok(Map.<String, Object>of(
                        "code", room.getCode(),
                        "participants", room.getParticipants(),
                        "roomState", room.getSharedState()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Join via a one-time invite code or reusable open code */
    @PostMapping("/join-via-code")
    public ResponseEntity<Map<String, Object>> joinViaCode(@RequestBody Map<String, String> body) {
        String shareCode = body.get("shareCode");
        String memberName = body.get("memberName");
        log.debug("POST /api/rooms/join-via-code — shareCode='{}', member='{}'", shareCode, memberName);
        if (shareCode == null || shareCode.isBlank() || memberName == null || memberName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "shareCode and memberName required"));
        }
        return roomService.joinViaCode(shareCode.trim(), memberName.trim())
                .map(room -> ResponseEntity.ok(Map.<String, Object>of(
                        "code", room.getCode(),
                        "participants", room.getParticipants(),
                        "roomState", room.getSharedState()
                )))
                .orElse(ResponseEntity.notFound().build());
    }

    /** Generate a one-time invite code (host only) */
    @PostMapping("/{code}/invite")
    public ResponseEntity<Map<String, Object>> generateInvite(@PathVariable String code) {
        log.debug("POST /api/rooms/{}/invite", code);
        RoomService.InviteCode invite = roomService.generateInviteCode(code);
        if (invite == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of(
                "inviteCode", invite.getCode(),
                "expiresAt", invite.getExpiresAt(),
                "ttlMs", RoomService.INVITE_TTL_MS
        ));
    }

    /** Get or create a reusable open code (host only) */
    @PostMapping("/{code}/open-code")
    public ResponseEntity<Map<String, Object>> getOpenCode(@PathVariable String code) {
        log.debug("POST /api/rooms/{}/open-code", code);
        String openCode = roomService.getOrCreateOpenCode(code);
        if (openCode == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("openCode", openCode));
    }

    @PostMapping("/leave")
    public ResponseEntity<Void> leaveRoom(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String memberName = body.get("memberName");
        log.debug("POST /api/rooms/leave — code='{}', member='{}'", code, memberName);
        if (code != null && memberName != null) {
            roomService.leaveRoom(code.trim(), memberName.trim());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{code}")
    public ResponseEntity<Map<String, Object>> getRoom(@PathVariable String code) {
        log.debug("GET /api/rooms/{}", code);
        return roomService.getRoom(code)
                .map(room -> ResponseEntity.ok(Map.<String, Object>of(
                        "code", room.getCode(),
                        "participants", room.getParticipants(),
                        "roomState", room.getSharedState()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
