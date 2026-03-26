package com.chawka.controller;

import com.chawka.model.Room;
import com.chawka.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomRestController {

    private final RoomService roomService;

    public RoomRestController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createRoom(@RequestBody Map<String, String> body) {
        String hostName = body.get("hostName");
        if (hostName == null || hostName.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "hostName required"));
        }
        Room room = roomService.createRoom(hostName.trim());
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

    @PostMapping("/leave")
    public ResponseEntity<Void> leaveRoom(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        String memberName = body.get("memberName");
        if (code != null && memberName != null) {
            roomService.leaveRoom(code.trim(), memberName.trim());
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{code}")
    public ResponseEntity<Map<String, Object>> getRoom(@PathVariable String code) {
        return roomService.getRoom(code)
                .map(room -> ResponseEntity.ok(Map.<String, Object>of(
                        "code", room.getCode(),
                        "participants", room.getParticipants(),
                        "roomState", room.getSharedState()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
