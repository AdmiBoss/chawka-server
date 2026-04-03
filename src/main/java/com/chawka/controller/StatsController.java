package com.chawka.controller;

import com.chawka.model.Room;
import com.chawka.service.DictionaryService;
import com.chawka.service.FileMetadataService;
import com.chawka.service.RoomService;
import com.chawka.service.ShareService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final RoomService roomService;
    private final DictionaryService dictionaryService;
    private final ShareService shareService;
    private final FileMetadataService fileMetadataService;

    public StatsController(RoomService roomService,
                           DictionaryService dictionaryService,
                           ShareService shareService,
                           FileMetadataService fileMetadataService) {
        this.roomService = roomService;
        this.dictionaryService = dictionaryService;
        this.shareService = shareService;
        this.fileMetadataService = fileMetadataService;
    }

    @GetMapping
    public Map<String, Object> stats() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("status", "UP");

        // Rooms
        result.put("roomCount", roomService.getRoomCount());
        result.put("totalParticipants", roomService.getTotalParticipants());

        // Rooms by type
        long hifdhRooms = 0;
        long daaretRooms = 0;
        for (Room room : roomService.getAllRooms()) {
            Map<String, Object> state = room.getSharedState();
            if (state != null && state.get("settings") instanceof Map<?, ?> settings) {
                Object groupType = settings.get("groupType");
                if ("hifdh".equals(groupType)) hifdhRooms++;
                else if ("daaret".equals(groupType)) daaretRooms++;
            }
        }
        Map<String, Long> roomsByType = new LinkedHashMap<>();
        roomsByType.put("hifdh", hifdhRooms);
        roomsByType.put("daaret", daaretRooms);
        result.put("roomsByType", roomsByType);

        // Dictionary
        result.put("dictionaryWords", dictionaryService.getAllGrouped().size());
        result.put("dictionaryEntries", dictionaryService.getAll().size());

        // Shared elements
        result.put("khotabCount", shareService.getAllKhotba().size());
        result.put("roqiaCount", shareService.getAllRoqia().size());
        result.put("filesCount", fileMetadataService.listRows().size());

        return result;
    }
}
