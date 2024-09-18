package com.inkneko.nekowindow.oss.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.oss.config.S3Config;
import com.inkneko.nekowindow.oss.entity.UploadRecord;
import com.inkneko.nekowindow.oss.maper.UploadRecordMapper;
import com.inkneko.nekowindow.oss.service.OssService;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Service
public class OssServiceImpl implements OssService {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    S3Config s3Config;
    S3Client s3Client;
    S3Presigner s3Presigner;
    UploadRecordMapper uploadRecordMapper;

    MinioClient minioClient;

    public OssServiceImpl(S3Config s3Config, S3Client s3Client, S3Presigner s3Presigner, UploadRecordMapper uploadRecordMapper, MinioClient minioClient) {
        this.s3Config = s3Config;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.uploadRecordMapper = uploadRecordMapper;
        this.minioClient = minioClient;
    }

    /**
     * 生成用于上传至指定位置的预签名链接
     *
     * @param bucket    桶
     * @param objectKey 对象key
     * @param uid       申请链接的用户id
     * @return 预签名连接
     * @throws ServiceException 若生成失败（通常情况下为目标已存在），产生该异常
     */
    @Override
    public String generatePreSignedUrl(String bucket, String objectKey, Long uid) {
        UploadRecord uploadRecord = new UploadRecord();
        uploadRecord.setBucket(bucket);
        uploadRecord.setObjectKey(objectKey);
        uploadRecord.setUrl(String.format("%s/%s/%s", s3Config.getEndpoint(), bucket, objectKey));
        uploadRecord.setUid(uid);
        uploadRecord.setEndpoint(s3Config.getEndpoint());
        uploadRecordMapper.insert(uploadRecord);
//        PresignedPutObjectRequest presignedPutObjectRequest =  s3Presigner.presignPutObject(
//                builder -> builder.signatureDuration(Duration.ofDays(3))
//                         .putObjectRequest(builder1 ->
//                                builder1.bucket(bucket)
//                                        .key(objectKey)
//                                        .build()
//                        )
//                        .build()
//                );
//
//        return presignedPutObjectRequest.url().toString();
        //由于生成的链接在使用时会提示签名错误，因此暂时使用minio client进行生成。
        // 目前怀疑是本地测试环境的host为localhost:9000，而aws s3的sdk不使用端口作为完整的host，导致生成错误的签名
        return genUrlByMinio(bucket, objectKey);
    }

    public String genUrlByMinio(String bucket, String objectKey) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()

                            .method(Method.PUT)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(3, TimeUnit.DAYS)
                            .build());
            return url;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            e.printStackTrace();
            throw new ServiceException(500, "对象存储服务错误，生成预签名链接失败");
        }
    }

    /**
     * 检查是否存在某个对象
     *
     * @param bucket    桶
     * @param objectKey 对象key
     * @return 若存在则返回上传记录，否则返回null
     */
    @Override
    public UploadRecord isObjectExists(String bucket, String objectKey) {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucket).key(objectKey).build());
            return uploadRecordMapper.selectOne(
                    new LambdaQueryWrapper<UploadRecord>()
                            .eq(UploadRecord::getBucket, bucket)
                            .eq(UploadRecord::getObjectKey, objectKey));
        } catch (NoSuchKeyException ignored) {
        }
        return null;
    }
}
