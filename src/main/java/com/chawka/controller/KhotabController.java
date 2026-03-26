package com.chawka.controller;

import com.chawka.model.KhotbaShare;
import com.chawka.service.ShareService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/khotab")
public class KhotabController {

    private final ShareService shareService;

    public KhotabController(ShareService shareService) {
        this.shareService = shareService;
    }

    @GetMapping
    public List<KhotbaShare> getAll() {
        return shareService.getAllKhotba();
    }

    @GetMapping("/{id}")
    public ResponseEntity<KhotbaShare> getOne(@PathVariable String id) {
        return shareService.getKhotba(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public KhotbaShare create(@RequestBody KhotbaShare share) {
        return shareService.saveKhotba(share);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (shareService.deleteKhotba(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
