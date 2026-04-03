package com.chawka.service;

import com.chawka.model.StoredFileRecord;
import com.chawka.repository.StoredFileRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileMetadataService {

    private static final Logger log = LoggerFactory.getLogger(FileMetadataService.class);
    private final StoredFileRecordRepository repo;

    public FileMetadataService(StoredFileRecordRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public StoredFileRecord saveNewRow(S3ObjectStorageService.StoredObjectInfo objectInfo) {
        log.debug("saveNewRow — file='{}', bucket='{}'", objectInfo.originalFilename(), objectInfo.bucket());
        long now = System.currentTimeMillis();
        StoredFileRecord row = new StoredFileRecord();
        row.setId(UUID.randomUUID().toString());
        row.setBucket(objectInfo.bucket());
        row.setObjectKey(objectInfo.objectKey());
        row.setOriginalFilename(objectInfo.originalFilename());
        row.setContentType(objectInfo.contentType());
        row.setSize(objectInfo.size());
        row.setCreatedAt(now);
        row.setUpdatedAt(now);
        row.setUserMetadata(objectInfo.userMetadata());
        return repo.save(row);
    }

    public List<StoredFileRecord> listRows() {
        log.debug("listRows");
        return repo.findAllByOrderByCreatedAtDesc();
    }

    public Optional<StoredFileRecord> getById(String id) {
        return repo.findById(id);
    }

    @Transactional
    public Optional<StoredFileRecord> deleteById(String id) {
        log.debug("deleteById('{}') — removing file record", id);
        Optional<StoredFileRecord> existing = repo.findById(id);
        existing.ifPresent(repo::delete);
        return existing;
    }
}
