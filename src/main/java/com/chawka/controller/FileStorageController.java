package com.chawka.controller;

import com.chawka.model.StoredFileRecord;
import com.chawka.service.FileMetadataService;
import com.chawka.service.S3ObjectStorageService;
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
        Map<String, String> userMetadata = extractUserMetadata(requestParams);
        S3ObjectStorageService.StoredObjectInfo storedObject = objectStorageService.upload(file, keyPrefix, userMetadata);
        return metadataService.saveNewRow(storedObject);
    }

    @GetMapping
    public List<StoredFileRecord> listFiles() {
        return metadataService.listRows();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoredFileRecord> getFileRow(@PathVariable String id) {
        return metadataService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/content")
    public ResponseEntity<InputStreamResource> getFileContent(@PathVariable String id) {
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
