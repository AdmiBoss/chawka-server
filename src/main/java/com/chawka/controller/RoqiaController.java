package com.chawka.controller;

import com.chawka.model.RoqiaShare;
import com.chawka.service.ShareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roqia")
public class RoqiaController {

    private static final Logger log = LoggerFactory.getLogger(RoqiaController.class);
    private final ShareService shareService;

    public RoqiaController(ShareService shareService) {
        this.shareService = shareService;
    }

    @GetMapping
    public List<RoqiaShare> getAll() {
        log.debug("GET /api/roqia — fetching all");
        return shareService.getAllRoqia();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoqiaShare> getOne(@PathVariable String id) {
        log.debug("GET /api/roqia/{}", id);
        return shareService.getRoqia(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public RoqiaShare create(@RequestBody RoqiaShare share) {
        log.debug("POST /api/roqia — creating new roqia");
        return shareService.saveRoqia(share);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.debug("DELETE /api/roqia/{}", id);
        if (shareService.deleteRoqia(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<RoqiaShare> incrementViews(@PathVariable String id) {
        log.debug("POST /api/roqia/{}/view", id);
        return shareService.incrementRoqiaViews(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
