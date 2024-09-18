package com.inkneko.nekowindow.oss.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

@Data
@Configuration
public class S3Config {
    @Value("${s3.access-key}")
    private String accessKey;

    @Value("${s3.secret-key}")
    private String secretKey;

    @Value("${s3.endpoint}")
    private String endpoint;

    @Bean
    public AwsCredentials awsCredentials() {
        return AwsBasicCredentials.create(accessKey, secretKey);
    }

    @Bean
    public S3Client s3Client(AwsCredentials credentials) {
        S3Client s3Client = S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("us-east-1"))
                .endpointOverride(URI.create(endpoint))
                .forcePathStyle(true)
                .build();
        initS3Backend(s3Client);
        return s3Client;
    }

    @Bean
    public S3Presigner s3Presigner(AwsCredentials credentials){
        return S3Presigner.builder()
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of("us-east-1"))
                .endpointOverride(URI.create(endpoint))
                .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                .build();
    }

    private void initS3Backend(S3Client s3Client) {
        try {
            s3Client.headBucket(HeadBucketRequest.builder().bucket("nekowindow").build());
        } catch (NoSuchBucketException e) {
            s3Client.createBucket(CreateBucketRequest.builder().bucket("nekowindow").build());
        }

        s3Client.putBucketPolicy(policy->{
            policy.bucket("nekowindow")
                    .policy("""
                            {
                              "Version": "2012-10-17",
                              "Statement": [
                                {
                                  "Effect": "Allow",
                                  "Principal": "*",
                                  "Action": "s3:GetObject",
                                  "Resource": "arn:aws:s3:::nekowindow/upload/cover/*"
                                },
                                {
                                  "Effect": "Allow",
                                  "Principal": "*",
                                  "Action": "s3:GetObject",
                                  "Resource": "arn:aws:s3:::nekowindow/upload/video/*"
                                }
                              ]
                            }""");
        });
    }

    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder().endpoint(endpoint).credentials(accessKey, secretKey).build();
    }

}
