package com.chawka.controller;

import com.chawka.model.StoredFileRecord;
import com.chawka.service.FileMetadataService;
import com.chawka.service.S3ObjectStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileStorageController {

    private static final Logger log = LoggerFactory.getLogger(FileStorageController.class);
    private final S3ObjectStorageService objectStorageService;
    private final FileMetadataService metadataService;

    public FileStorageController(S3ObjectStorageService objectStorageService, FileMetadataService metadataService) {
        this.objectStorageService = objectStorageService;
        this.metadataService = metadataService;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public StoredFileRecord uploadFile(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "keyPrefix", required = false) String keyPrefix,
            @RequestParam MultiValueMap<String, String> requestParams
    ) {
        log.debug("POST /api/files — upload file='{}', size={}, keyPrefix='{}'", file.getOriginalFilename(), file.getSize(), keyPrefix);
        Map<String, String> userMetadata = extractUserMetadata(requestParams);
        S3ObjectStorageService.StoredObjectInfo storedObject = objectStorageService.upload(file, keyPrefix, userMetadata);
        log.debug("File uploaded to S3: bucket='{}', key='{}'", storedObject.bucket(), storedObject.objectKey());
        return metadataService.saveNewRow(storedObject);
    }

    @GetMapping
    public List<StoredFileRecord> listFiles() {
        log.debug("GET /api/files — listing all files");
        return metadataService.listRows();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoredFileRecord> getFileRow(@PathVariable String id) {
        log.debug("GET /api/files/{}", id);
        return metadataService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<InputStreamResource> getFileContent(@PathVariable String id) {
        log.debug("GET /api/files/{}/content", id);
        return metadataService.getById(id)
                .map(row -> {
                    ResponseInputStream<GetObjectResponse> objectStream = objectStorageService.getObjectStream(
                            row.getBucket(),
                            row.getObjectKey()
                    );

                    MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
                    if (row.getContentType() != null && !row.getContentType().isBlank()) {
                        try {
                            mediaType = MediaType.parseMediaType(row.getContentType());
                        } catch (Exception ignored) {
                            mediaType = MediaType.APPLICATION_OCTET_STREAM;
                        }
                    }

                    return ResponseEntity.ok()
                            .contentType(mediaType)
                            .contentLength(row.getSize())
                            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + row.getOriginalFilename() + "\"")
                            .body(new InputStreamResource(objectStream));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFile(@PathVariable String id) {
        log.debug("DELETE /api/files/{}", id);
        return metadataService.deleteById(id)
                .map(row -> {
                    objectStorageService.delete(row.getBucket(), row.getObjectKey());
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, String> extractUserMetadata(MultiValueMap<String, String> params) {
        Map<String, String> metadata = new HashMap<>();
        if (params == null || params.isEmpty()) {
            return metadata;
        }

        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
            String key = entry.getKey();
            if (key == null || !key.startsWith("meta.")) {
                continue;
            }
            List<String> values = entry.getValue();
            if (values == null || values.isEmpty()) {
                continue;
            }
            String metadataKey = key.substring("meta.".length());
            String value = values.get(0);
            if (!metadataKey.isBlank() && value != null && !value.isBlank()) {
                metadata.put(metadataKey, value);
            }
        }
        return metadata;
    }
}
