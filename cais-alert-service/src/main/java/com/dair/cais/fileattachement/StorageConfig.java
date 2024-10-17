package com.dair.cais.fileattachement;

import com.amazonaws.services.s3.AmazonS3;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Value("${storage.type}")
    private String storageType;

    @Value("${storage.location:#{null}}")
    private String storageLocation;

    @Value("${storage.s3.bucket-name:#{null}}")
    private String s3BucketName;

    @Bean
    public StorageService storageService(AmazonS3 amazonS3) {
        if ("s3".equalsIgnoreCase(storageType)) {
            return new S3StorageService(amazonS3, s3BucketName);
        } else {
            return new LocalFileStorageService(storageLocation);
        }
    }
}