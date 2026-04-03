package com.chawka.service;

import com.chawka.model.KhotbaShare;
import com.chawka.model.RoqiaShare;
import com.chawka.repository.KhotbaShareRepository;
import com.chawka.repository.RoqiaShareRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(ShareService.class)
class ShareServiceTest {

    @Autowired
    private ShareService shareService;

    // ── Roqia ──

    @Test
    void saveRoqia_generatesIdAndPersists() {
        RoqiaShare share = new RoqiaShare();
        share.setTitle("Test Roqia");
        share.setSharedBy("Ali");
        share.setSharedDate("2026-04-01");

        RoqiaShare saved = shareService.saveRoqia(share);
        assertNotNull(saved.getId());
        assertTrue(saved.getId().startsWith("roqia-"));
        assertTrue(saved.getCreatedAt() > 0);
    }

    @Test
    void getAllRoqia_returnsAll() {
        RoqiaShare s1 = new RoqiaShare();
        s1.setTitle("R1");
        s1.setSharedDate("2026-04-01");
        shareService.saveRoqia(s1);

        RoqiaShare s2 = new RoqiaShare();
        s2.setTitle("R2");
        s2.setSharedDate("2026-04-02");
        shareService.saveRoqia(s2);

        List<RoqiaShare> all = shareService.getAllRoqia();
        assertEquals(2, all.size());
    }

    @Test
    void getRoqia_existingId_returnsShare() {
        RoqiaShare s = new RoqiaShare();
        s.setTitle("R1");
        s.setSharedDate("2026-04-01");
        RoqiaShare saved = shareService.saveRoqia(s);

        Optional<RoqiaShare> found = shareService.getRoqia(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("R1", found.get().getTitle());
    }

    @Test
    void deleteRoqia_existingId_returnsTrue() {
        RoqiaShare s = new RoqiaShare();
        s.setTitle("R1");
        s.setSharedDate("2026-04-01");
        RoqiaShare saved = shareService.saveRoqia(s);

        assertTrue(shareService.deleteRoqia(saved.getId()));
        assertTrue(shareService.getRoqia(saved.getId()).isEmpty());
    }

    @Test
    void deleteRoqia_nonExistent_returnsFalse() {
        assertFalse(shareService.deleteRoqia("bad-id"));
    }

    @Test
    void incrementRoqiaViews_incrementsCount() {
        RoqiaShare s = new RoqiaShare();
        s.setTitle("R1");
        s.setSharedDate("2026-04-01");
        RoqiaShare saved = shareService.saveRoqia(s);

        Optional<RoqiaShare> updated = shareService.incrementRoqiaViews(saved.getId());
        assertTrue(updated.isPresent());
        assertEquals(1, updated.get().getViewCount());
    }

    // ── Khotba ──

    @Test
    void saveKhotba_generatesIdAndPersists() {
        KhotbaShare share = new KhotbaShare();
        share.setTitle("Test Khotba");
        share.setMosque("Masjid Test");
        share.setSharedDate("2026-04-01");

        KhotbaShare saved = shareService.saveKhotba(share);
        assertNotNull(saved.getId());
        assertTrue(saved.getId().startsWith("khotba-"));
        assertTrue(saved.getCreatedAt() > 0);
    }

    @Test
    void getAllKhotba_returnsAll() {
        KhotbaShare k1 = new KhotbaShare();
        k1.setTitle("K1");
        k1.setSharedDate("2026-04-01");
        shareService.saveKhotba(k1);

        KhotbaShare k2 = new KhotbaShare();
        k2.setTitle("K2");
        k2.setSharedDate("2026-04-02");
        shareService.saveKhotba(k2);

        assertEquals(2, shareService.getAllKhotba().size());
    }

    @Test
    void getKhotba_existingId_returnsShare() {
        KhotbaShare k = new KhotbaShare();
        k.setTitle("K1");
        k.setSharedDate("2026-04-01");
        KhotbaShare saved = shareService.saveKhotba(k);

        Optional<KhotbaShare> found = shareService.getKhotba(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("K1", found.get().getTitle());
    }

    @Test
    void deleteKhotba_existingId_returnsTrue() {
        KhotbaShare k = new KhotbaShare();
        k.setTitle("K1");
        k.setSharedDate("2026-04-01");
        KhotbaShare saved = shareService.saveKhotba(k);

        assertTrue(shareService.deleteKhotba(saved.getId()));
        assertTrue(shareService.getKhotba(saved.getId()).isEmpty());
    }

    @Test
    void deleteKhotba_nonExistent_returnsFalse() {
        assertFalse(shareService.deleteKhotba("bad-id"));
    }
}
