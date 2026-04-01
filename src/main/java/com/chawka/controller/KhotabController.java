package com.chawka.controller;

import com.chawka.model.KhotbaShare;
import com.chawka.service.ShareService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/khotab")
public class KhotabController {

    private static final Logger log = LoggerFactory.getLogger(KhotabController.class);
    private final ShareService shareService;

    public KhotabController(ShareService shareService) {
        this.shareService = shareService;
    }

    @GetMapping
    public List<KhotbaShare> getAll() {
        log.debug("GET /api/khotab — fetching all");
        return shareService.getAllKhotba();
    }

    @GetMapping("/{id}")
    public ResponseEntity<KhotbaShare> getOne(@PathVariable String id) {
        log.debug("GET /api/khotab/{}", id);
        return shareService.getKhotba(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public KhotbaShare create(@RequestBody KhotbaShare share) {
        log.debug("POST /api/khotab — creating new khotba");
        return shareService.saveKhotba(share);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        log.debug("DELETE /api/khotab/{}", id);
        if (shareService.deleteKhotba(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
