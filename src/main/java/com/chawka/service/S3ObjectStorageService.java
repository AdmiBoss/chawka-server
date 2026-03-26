package com.chawka.service;

import com.chawka.config.S3StorageProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.ResponseInputStream;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class S3ObjectStorageService {

    public record StoredObjectInfo(String bucket, String objectKey, String originalFilename, String contentType, long size,
                                   Map<String, String> userMetadata) {
    }

    private final S3Client s3Client;
    private final S3StorageProperties properties;

    public S3ObjectStorageService(S3Client s3Client, S3StorageProperties properties) {
        this.s3Client = s3Client;
        this.properties = properties;
    }

    public StoredObjectInfo upload(MultipartFile file, String requestedPrefix, Map<String, String> userMetadata) {
        String bucket = requireBucket();
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        long size = file.getSize();

        String keyPrefix = normalizePrefix(requestedPrefix != null && !requestedPrefix.isBlank()
                ? requestedPrefix
                : properties.getKeyPrefix());
        String objectKey = buildObjectKey(keyPrefix, originalFilename);
        Map<String, String> cleanedMetadata = sanitizeMetadata(userMetadata);

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .contentType(contentType)
                    .contentLength(size)
                    .metadata(cleanedMetadata)
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));
            return new StoredObjectInfo(bucket, objectKey, originalFilename, contentType, size, cleanedMetadata);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read uploaded file bytes", e);
        }
    }

    public void delete(String bucket, String objectKey) {
        if (bucket == null || bucket.isBlank() || objectKey == null || objectKey.isBlank()) {
            return;
        }
        DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();
        s3Client.deleteObject(deleteRequest);
    }

    public ResponseInputStream<GetObjectResponse> getObjectStream(String bucket, String objectKey) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();
        return s3Client.getObject(request);
    }

    private String requireBucket() {
        String bucket = properties.getBucket();
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalStateException("S3 bucket is not configured. Set app.storage.s3.bucket");
        }
        return bucket;
    }

    private String normalizePrefix(String prefix) {
        String value = prefix == null ? "" : prefix.trim();
        value = value.replace('\\', '/');
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String buildObjectKey(String prefix, String originalFilename) {
        LocalDate now = LocalDate.now();
        String safeName = sanitizeFilename(originalFilename);
        String filePart = UUID.randomUUID() + "-" + safeName;
        String datedPath = now.getYear() + "/" + now.getMonthValue() + "/" + now.getDayOfMonth();

        if (prefix == null || prefix.isBlank()) {
            return datedPath + "/" + filePart;
        }
        return prefix + "/" + datedPath + "/" + filePart;
    }

    private String sanitizeFilename(String originalFilename) {
        String safe = originalFilename.replace('\\', '/');
        int slash = safe.lastIndexOf('/');
        if (slash >= 0) {
            safe = safe.substring(slash + 1);
        }
        safe = safe.replaceAll("[^a-zA-Z0-9._-]", "_");
        return safe.isBlank() ? "file" : safe;
    }

    private Map<String, String> sanitizeMetadata(Map<String, String> metadata) {
        Map<String, String> cleaned = new HashMap<>();
        if (metadata == null || metadata.isEmpty()) {
            return cleaned;
        }

        for (Map.Entry<String, String> entry : metadata.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (key == null || key.isBlank() || value == null) {
                continue;
            }
            String normalizedKey = key.trim().toLowerCase().replaceAll("[^a-z0-9_-]", "-");
            if (!normalizedKey.isBlank()) {
                cleaned.put(normalizedKey, value);
            }
        }
        return cleaned;
    }
}
