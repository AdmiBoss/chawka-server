package com.chawka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class S3ClientConfig {

    @Bean
    public S3Client s3Client(S3StorageProperties properties) {
        String configuredRegion = properties.getRegion();
        if (configuredRegion != null && !configuredRegion.isBlank()) {
            return S3Client.builder().region(Region.of(configuredRegion)).build();
        }

        try {
            Region detected = DefaultAwsRegionProviderChain.builder().build().getRegion();
            return S3Client.builder().region(detected).build();
        } catch (Exception ignored) {
            return S3Client.builder().region(Region.US_EAST_1).build();
        }
    }
}
