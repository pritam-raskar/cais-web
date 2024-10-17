package com.dair.cais.fileattachement;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class S3StorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(S3StorageService.class);

    private final AmazonS3 s3Client;
    private final String bucketName;

    @Autowired
    public S3StorageService(AmazonS3 s3Client, @Value("${storage.s3.bucket-name}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    @Override
    public String store(String alertId, String filename, InputStream inputStream) throws IOException {
        String key = alertId + "/" + filename;
        ObjectMetadata metadata = new ObjectMetadata();
        s3Client.putObject(bucketName, key, inputStream, metadata);
        return s3Client.getUrl(bucketName, key).toString();
    }

    @Override
    public byte[] retrieve(String alertId, String filename) throws IOException {
        String key = alertId + "/" + filename;
        S3Object object = s3Client.getObject(bucketName, key);
        return object.getObjectContent().readAllBytes();
    }

    @Override
    public List<String> listByAlertId(String alertId) {
        try {
            return s3Client.listObjects(bucketName, alertId + "/")
                    .getObjectSummaries().stream()
                    .map(S3ObjectSummary::getKey)
                    .map(key -> key.substring(key.lastIndexOf('/') + 1))
                    .collect(Collectors.toList());
        } catch (AmazonServiceException e) {
            logger.error("Error listing files from S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to list files from S3", e);
        }
    }

//    @Override
//    public byte[] retrieve(String alertId, String filename) throws IOException {
//        try {
//            S3Object object = s3Client.getObject(bucketName, alertId + "/" + filename);
//            try (InputStream is = object.getObjectContent()) {
//                return is.readAllBytes();
//            }
//        } catch (AmazonServiceException e) {
//            logger.error("Error retrieving file from S3: {}", e.getMessage(), e);
//            throw new IOException("Failed to retrieve file from S3", e);
//        }
//    }
}