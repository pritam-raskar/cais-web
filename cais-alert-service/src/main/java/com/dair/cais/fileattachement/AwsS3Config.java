package com.dair.cais.fileattachement;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class AwsS3Config {

    @Value("${cloud.aws.region.static:us-east-1}")
    private String awsRegion;

    @Value("${cloud.aws.credentials.access-key:#{null}}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key:#{null}}")
    private String secretKey;

    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "s3")
    public AmazonS3 amazonS3Client() {
        AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
                .withRegion(awsRegion);

        // Use explicit credentials if provided, otherwise use default provider chain
        if (StringUtils.hasText(accessKey) && StringUtils.hasText(secretKey)) {
            BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
            builder.withCredentials(new AWSStaticCredentialsProvider(awsCredentials));
        } else {
            builder.withCredentials(DefaultAWSCredentialsProviderChain.getInstance());
        }

        return builder.build();
    }
}