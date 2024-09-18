package com.inkneko.nekowindow.oss.service;

import com.inkneko.nekowindow.oss.config.S3Config;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class OssServiceTests {
    @Autowired
    S3Client s3Client;
    @Autowired
    S3Config s3Config;

    @Test
    void testStat(){
        try {
            HeadObjectResponse response = s3Client.headObject(HeadObjectRequest.builder().bucket("nekowindow").key("video_upload/testfile").build());
            System.out.println("exists");
        }catch (NoSuchKeyException e){
            System.out.println("not exists");
        }
    }

    @Test
    void testMinio(){
        MinioClient minioClient = MinioClient.builder().endpoint(s3Config.getEndpoint()).credentials(s3Config.getAccessKey(), s3Config.getSecretKey()).build();
        try {
            String url =  minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket("nekowindow")
                            .object("fuckshit")
                            .expiry(3, TimeUnit.DAYS)
                            .build());
            System.out.println(url);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            e.printStackTrace();
        }
    }
}
