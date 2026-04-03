package com.chawka.service;

import com.chawka.model.KhotbaShare;
import com.chawka.model.RoqiaShare;
import com.chawka.repository.KhotbaShareRepository;
import com.chawka.repository.RoqiaShareRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ShareService {

    private static final Logger log = LoggerFactory.getLogger(ShareService.class);

    private final RoqiaShareRepository roqiaRepo;
    private final KhotbaShareRepository khotbaRepo;

    public ShareService(RoqiaShareRepository roqiaRepo, KhotbaShareRepository khotbaRepo) {
        this.roqiaRepo = roqiaRepo;
        this.khotbaRepo = khotbaRepo;
    }

    // ── Roqia ──

    public List<RoqiaShare> getAllRoqia() {
        return roqiaRepo.findAllByOrderBySharedDateDescCreatedAtDesc();
    }

    public Optional<RoqiaShare> getRoqia(String id) {
        return roqiaRepo.findById(id);
    }

    @Transactional
    public RoqiaShare saveRoqia(RoqiaShare share) {
        if (share.getId() == null || share.getId().isBlank()) {
            share.setId("roqia-" + System.currentTimeMillis() + "-" + randomSuffix());
        }
        if (share.getCreatedAt() == 0) {
            share.setCreatedAt(System.currentTimeMillis());
        }
        log.debug("saveRoqia id='{}'", share.getId());
        return roqiaRepo.save(share);
    }

    @Transactional
    public boolean deleteRoqia(String id) {
        if (roqiaRepo.existsById(id)) {
            roqiaRepo.deleteById(id);
            return true;
        }
        return false;
    }

    @Transactional
    public Optional<RoqiaShare> incrementRoqiaViews(String id) {
        return roqiaRepo.findById(id).map(share -> {
            share.setViewCount(share.getViewCount() + 1);
            return roqiaRepo.save(share);
        });
    }

    // ── Khotba ──

    public List<KhotbaShare> getAllKhotba() {
        return khotbaRepo.findAllByOrderBySharedDateDescCreatedAtDesc();
    }

    public Optional<KhotbaShare> getKhotba(String id) {
        return khotbaRepo.findById(id);
    }

    @Transactional
    public KhotbaShare saveKhotba(KhotbaShare share) {
        if (share.getId() == null || share.getId().isBlank()) {
            share.setId("khotba-" + System.currentTimeMillis() + "-" + randomSuffix());
        }
        if (share.getCreatedAt() == 0) {
            share.setCreatedAt(System.currentTimeMillis());
        }
        log.debug("saveKhotba id='{}'", share.getId());
        return khotbaRepo.save(share);
    }

    @Transactional
    public boolean deleteKhotba(String id) {
        if (khotbaRepo.existsById(id)) {
            khotbaRepo.deleteById(id);
            return true;
        }
        return false;
    }

    private String randomSuffix() {
        return Long.toString(Math.abs(new Random().nextLong()), 36).substring(0, 6);
    }
}
