package com.chawka.service;

import com.chawka.model.StoredFileRecord;
import com.chawka.repository.StoredFileRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@Import(FileMetadataService.class)
class FileMetadataServiceTest {

    @Autowired
    private FileMetadataService fileMetadataService;

    @Autowired
    private StoredFileRecordRepository repo;

    private S3ObjectStorageService.StoredObjectInfo makeInfo(String filename) {
        return new S3ObjectStorageService.StoredObjectInfo(
                "test-bucket", "uploads/" + filename, filename,
                "audio/mpeg", 1024L, Map.of("uploader", "Ali")
        );
    }

    @Test
    void saveNewRow_persistsRecord() {
        StoredFileRecord row = fileMetadataService.saveNewRow(makeInfo("test.mp3"));
        assertNotNull(row.getId());
        assertEquals("test-bucket", row.getBucket());
        assertEquals("test.mp3", row.getOriginalFilename());
        assertEquals("audio/mpeg", row.getContentType());
        assertEquals(1024L, row.getSize());
        assertEquals("Ali", row.getUserMetadata().get("uploader"));
    }

    @Test
    void listRows_returnsAllOrderedByCreatedAtDesc() {
        fileMetadataService.saveNewRow(makeInfo("a.mp3"));
        fileMetadataService.saveNewRow(makeInfo("b.mp3"));

        List<StoredFileRecord> rows = fileMetadataService.listRows();
        assertEquals(2, rows.size());
    }

    @Test
    void getById_existingId_returnsRecord() {
        StoredFileRecord row = fileMetadataService.saveNewRow(makeInfo("test.mp3"));
        Optional<StoredFileRecord> found = fileMetadataService.getById(row.getId());
        assertTrue(found.isPresent());
        assertEquals(row.getId(), found.get().getId());
    }

    @Test
    void getById_unknownId_returnsEmpty() {
        assertTrue(fileMetadataService.getById("nonexistent").isEmpty());
    }

    @Test
    void deleteById_existingId_removesRecord() {
        StoredFileRecord row = fileMetadataService.saveNewRow(makeInfo("test.mp3"));
        Optional<StoredFileRecord> deleted = fileMetadataService.deleteById(row.getId());
        assertTrue(deleted.isPresent());
        assertTrue(fileMetadataService.getById(row.getId()).isEmpty());
    }

    @Test
    void deleteById_unknownId_returnsEmpty() {
        assertTrue(fileMetadataService.deleteById("nonexistent").isEmpty());
    }
}
