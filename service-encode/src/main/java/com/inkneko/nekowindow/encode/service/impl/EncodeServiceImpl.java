package com.inkneko.nekowindow.encode.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.common.util.OssUtils;
import com.inkneko.nekowindow.encode.config.EncodeConfig;
import com.inkneko.nekowindow.encode.entity.EncodeRequest;
import com.inkneko.nekowindow.encode.entity.ProbeResult;
import com.inkneko.nekowindow.encode.service.EncodeService;
import com.mysql.cj.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.UUID;

@Service
public class EncodeServiceImpl implements EncodeService {
    Logger logger = LoggerFactory.getLogger(EncodeServiceImpl.class);

    @Autowired
    S3Client s3Client;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Override
    public void parseSourceVideo(String url) {
        rabbitTemplate.convertAndSend(EncodeConfig.topicExchangeName, "encode.file_id.233", new EncodeRequest(0L, url));
    }

    @Override
    public void encodeVideo(String sourceVideoUrl) {
        ObjectMapper objectMapper = new ObjectMapper();
        //查询视频链接，并下载视频文件
        OssUtils.OssLink ossLink = OssUtils.url(sourceVideoUrl);
        if (ossLink == null){
            throw new ServiceException(400, "视频链接格式错误");
        }
        File tempFile = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        s3Client.getObject(request-> request.bucket(ossLink.bucket).key(ossLink.key), Path.of(tempFile.toURI()));

        //调用ffprobe分析视频格式
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffprobe",
                "-v", "quiet",
                "-of", "json",
                "-show_format",
                "-show_streams",
                tempFile.getAbsolutePath()
        );

        try {
            Process process = processBuilder.start();
            int retCode = process.waitFor();
            if (retCode == 0) {
                String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                ProbeResult probeResult = objectMapper.readValue(output, ProbeResult.class);
                logger.info(probeResult.toString());
            }
        }catch (Exception e){
            logger.error("error", e);
        }

        tempFile.deleteOnExit();
    }


}
