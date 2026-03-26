package com.chawka.controller;

import com.chawka.model.RoqiaShare;
import com.chawka.service.ShareService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roqia")
public class RoqiaController {

    private final ShareService shareService;

    public RoqiaController(ShareService shareService) {
        this.shareService = shareService;
    }

    @GetMapping
    public List<RoqiaShare> getAll() {
        return shareService.getAllRoqia();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoqiaShare> getOne(@PathVariable String id) {
        return shareService.getRoqia(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public RoqiaShare create(@RequestBody RoqiaShare share) {
        return shareService.saveRoqia(share);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (shareService.deleteRoqia(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<RoqiaShare> incrementViews(@PathVariable String id) {
        return shareService.incrementRoqiaViews(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
