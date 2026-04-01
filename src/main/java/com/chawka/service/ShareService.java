package com.chawka.service;

import com.chawka.model.KhotbaShare;
import com.chawka.model.RoqiaShare;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ShareService {

    private static final Logger log = LoggerFactory.getLogger(ShareService.class);

    private final Map<String, RoqiaShare> roqiaShares = new ConcurrentHashMap<>();
    private final Map<String, KhotbaShare> khotbaShares = new ConcurrentHashMap<>();

    // ── Roqia ──

    public List<RoqiaShare> getAllRoqia() {
        List<RoqiaShare> list = new ArrayList<>(roqiaShares.values());
        list.sort(Comparator.comparing(RoqiaShare::getSharedDate, Comparator.reverseOrder())
                .thenComparing(Comparator.comparingLong(RoqiaShare::getCreatedAt).reversed()));
        return list;
    }

    public Optional<RoqiaShare> getRoqia(String id) {
        return Optional.ofNullable(roqiaShares.get(id));
    }

    public RoqiaShare saveRoqia(RoqiaShare share) {
        if (share.getId() == null || share.getId().isBlank()) {
            share.setId("roqia-" + System.currentTimeMillis() + "-" + randomSuffix());
        }
        if (share.getCreatedAt() == 0) {
            share.setCreatedAt(System.currentTimeMillis());
        }
        roqiaShares.put(share.getId(), share);
        log.debug("saveRoqia id='{}'", share.getId());
        return share;
    }

    public boolean deleteRoqia(String id) {
        return roqiaShares.remove(id) != null;
    }

    public Optional<RoqiaShare> incrementRoqiaViews(String id) {
        RoqiaShare share = roqiaShares.get(id);
        if (share != null) {
            share.setViewCount(share.getViewCount() + 1);
            return Optional.of(share);
        }
        return Optional.empty();
    }

    // ── Khotba ──

    public List<KhotbaShare> getAllKhotba() {
        List<KhotbaShare> list = new ArrayList<>(khotbaShares.values());
        list.sort(Comparator.comparing(KhotbaShare::getSharedDate, Comparator.reverseOrder())
                .thenComparing(Comparator.comparingLong(KhotbaShare::getCreatedAt).reversed()));
        return list;
    }

    public Optional<KhotbaShare> getKhotba(String id) {
        return Optional.ofNullable(khotbaShares.get(id));
    }

    public KhotbaShare saveKhotba(KhotbaShare share) {
        if (share.getId() == null || share.getId().isBlank()) {
            share.setId("khotba-" + System.currentTimeMillis() + "-" + randomSuffix());
        }
        if (share.getCreatedAt() == 0) {
            share.setCreatedAt(System.currentTimeMillis());
        }
        khotbaShares.put(share.getId(), share);
        log.debug("saveKhotba id='{}'", share.getId());
        return share;
    }

    public boolean deleteKhotba(String id) {
        return khotbaShares.remove(id) != null;
    }

    private String randomSuffix() {
        return Long.toString(Math.abs(new Random().nextLong()), 36).substring(0, 6);
    }
}
