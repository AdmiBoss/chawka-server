package com.chawka.controller;

import com.chawka.model.Room;
import com.chawka.service.RoomService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class RoomWsController {

    private final SimpMessagingTemplate messaging;
    private final RoomService roomService;

    public RoomWsController(SimpMessagingTemplate messaging, RoomService roomService) {
        this.messaging = messaging;
        this.roomService = roomService;
    }

    /**
     * Client sends a chat message to a room.
     * Destination: /app/room/{code}/chat
     * Broadcast to: /topic/room/{code}
     */
    @MessageMapping("/room/{code}/chat")
    public void handleChat(@DestinationVariable String code, @Payload Map<String, Object> message) {
        message.put("type", "chat");
        messaging.convertAndSend("/topic/room/" + code, message);
    }

    /**
     * Client notifies join.
     * Destination: /app/room/{code}/join
     */
    @MessageMapping("/room/{code}/join")
    public void handleJoin(@DestinationVariable String code, @Payload Map<String, Object> message) {
        String memberName = (String) message.get("from");
        if (memberName != null) {
            roomService.joinRoom(code, memberName);
        }

        message.put("type", "join");
        messaging.convertAndSend("/topic/room/" + code, message);

        // Also broadcast updated room state
        roomService.getRoom(code).ifPresent(room -> {
            Map<String, Object> stateMsg = Map.of(
                    "type", "room-state",
                    "from", "server",
                    "participants", room.getParticipants(),
                    "roomState", room.getSharedState()
            );
            messaging.convertAndSend("/topic/room/" + code, stateMsg);
        });
    }

    /**
     * Client notifies leave.
     * Destination: /app/room/{code}/leave
     */
    @MessageMapping("/room/{code}/leave")
    public void handleLeave(@DestinationVariable String code, @Payload Map<String, Object> message) {
        String memberName = (String) message.get("from");
        if (memberName != null) {
            roomService.leaveRoom(code, memberName);
        }

        message.put("type", "leave");
        messaging.convertAndSend("/topic/room/" + code, message);

        // Broadcast updated state if room still exists
        roomService.getRoom(code).ifPresent(room -> {
            Map<String, Object> stateMsg = Map.of(
                    "type", "room-state",
                    "from", "server",
                    "participants", room.getParticipants(),
                    "roomState", room.getSharedState()
            );
            messaging.convertAndSend("/topic/room/" + code, stateMsg);
        });
    }

    /**
     * Client broadcasts a full room-state update (host only).
     * Destination: /app/room/{code}/state
     */
    @SuppressWarnings("unchecked")
    @MessageMapping("/room/{code}/state")
    public void handleState(@DestinationVariable String code, @Payload Map<String, Object> message) {
        Object roomStateObj = message.get("roomState");
        if (roomStateObj instanceof Map) {
            roomService.updateRoomState(code, (Map<String, Object>) roomStateObj);
        }
        message.put("type", "room-state");
        messaging.convertAndSend("/topic/room/" + code, message);
    }

    /**
     * Hifdh submission relay (member -> host, then host validates and broadcasts).
     * Destination: /app/room/{code}/hifdh-submit
     */
    @MessageMapping("/room/{code}/hifdh-submit")
    public void handleHifdhSubmit(@DestinationVariable String code, @Payload Map<String, Object> message) {
        message.put("type", "hifdh-submit");
        messaging.convertAndSend("/topic/room/" + code, message);
    }
}
