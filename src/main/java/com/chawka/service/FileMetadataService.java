package com.chawka.service;

import com.chawka.model.StoredFileRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileMetadataService {

    private static final Logger log = LoggerFactory.getLogger(FileMetadataService.class);
    private final Map<String, StoredFileRecord> rows = new ConcurrentHashMap<>();

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

        rows.put(row.getId(), row);
        return row;
    }

    public List<StoredFileRecord> listRows() {
        log.debug("listRows — {} files stored", rows.size());
        List<StoredFileRecord> list = new ArrayList<>(rows.values());
        list.sort(Comparator.comparingLong(StoredFileRecord::getCreatedAt).reversed());
        return list;
    }

    public Optional<StoredFileRecord> getById(String id) {
        return Optional.ofNullable(rows.get(id));
    }

    public Optional<StoredFileRecord> deleteById(String id) {
        log.debug("deleteById('{}') — removing file record", id);
        return Optional.ofNullable(rows.remove(id));
    }
}
