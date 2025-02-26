package com.inkneko.nekowindow.encode.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkneko.nekowindow.api.video.client.VideoFeignClient;
import com.inkneko.nekowindow.api.video.dto.UpdateVideoResourceConversionStateDTO;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.encode.config.EncodeConfig;
import com.inkneko.nekowindow.encode.config.S3Config;
import com.inkneko.nekowindow.encode.dto.ConcatRequestDTO;
import com.inkneko.nekowindow.encode.dto.VideoSegmentEncodeDTO;
import com.inkneko.nekowindow.encode.entity.AudioEncodeTask;
import com.inkneko.nekowindow.encode.entity.VideoEncodeTask;
import com.inkneko.nekowindow.encode.service.EncodeService;
import com.inkneko.nekowindow.encode.util.MediaUtils;
import com.rabbitmq.client.Channel;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;

@Component
@Slf4j
public class ConcatConsumer {
    @Autowired
    EncodeService encodeService;

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    MinioClient minioClient;

    @Autowired
    S3Config s3Config;

    @Autowired
    VideoFeignClient videoFeignClient;

    @RabbitListener(queues = EncodeConfig.NK_CONCAT_QUEUE_NAME, ackMode = "MANUAL")
    public void concat(Channel channel, Message message) {
        ObjectMapper objectMapper = new ObjectMapper();
        ConcatRequestDTO concatRequestDTO;
        try {
            concatRequestDTO = objectMapper.readValue(message.getBody(), ConcatRequestDTO.class);
        } catch (IOException e) {
            log.error("转换ConcatRequestDTO时发生IOException: ", e);

            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (IOException e2) {
                log.error("消息ack时发生IOException: ", e2);
                return;
            }
            return;
        }

        log.info("收到合并请求：{}", concatRequestDTO);


        //1. 查询指定videoId的转码任务，包括视频与音频任务，根据质量代码进行分类
        List<VideoEncodeTask> videoEncodeTasks = encodeService.getVideoEncodeTasks(concatRequestDTO.getVideoId());
        Map<Integer, List<VideoEncodeTask>> qualityVideoEncodeTasks = new HashMap<>();
        for (VideoEncodeTask videoEncodeTask : videoEncodeTasks) {
            qualityVideoEncodeTasks.putIfAbsent(videoEncodeTask.getVideoQualityCode(), new ArrayList<>());
            qualityVideoEncodeTasks.get(videoEncodeTask.getVideoQualityCode()).add(videoEncodeTask);
        }
        List<AudioEncodeTask> audioEncodeTasks = encodeService.getAudioEncodeTasks(concatRequestDTO.getVideoId()).stream().sorted().toList();

        //2. 对每个视频质量代码的视频进行合并
        Map<Integer, File> qualityVideoFilesMap = new HashMap<>();
        for (Integer qualityCode : qualityVideoEncodeTasks.keySet().stream().sorted().toList()) {
            List<VideoEncodeTask> qualityVideoEncodeTaskList = qualityVideoEncodeTasks.get(qualityCode);
            //根据分段进行排序
            qualityVideoEncodeTaskList.sort(Comparator.comparingInt(VideoEncodeTask::getSegmentIndex));
            try {
                File inputVideosTxtFile = File.createTempFile("concat_info-%d-%d-".formatted(concatRequestDTO.getVideoId(), qualityCode), "txt");
                try (FileWriter fileWriter = new FileWriter(inputVideosTxtFile)) {
                    StringBuilder stringBuilder = new StringBuilder();
                    for (VideoEncodeTask videoEncodeTask : qualityVideoEncodeTaskList) {
                        stringBuilder.append("file %s\n".formatted(videoEncodeTask.getResultVideoUrl()));
                    }
                    fileWriter.write(stringBuilder.toString());
                }
                File outputFile = File.createTempFile("%d-%d-".formatted(concatRequestDTO.getVideoId(), qualityCode), "mp4");
                try {
                    MediaUtils.concatVideo(inputVideosTxtFile, outputFile);
                    qualityVideoFilesMap.put(qualityCode, outputFile);
                } catch (InterruptedException interruptedException) {
                    log.error("视频合并时被中断：", interruptedException);
                    return;
                } catch (ServiceException serviceException) {
                    log.error("视频合并时出现业务异常：", serviceException);
                    try {
                        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                    } catch (IOException e) {
                        log.error("消息ack时发生IOException: ", e);
                        return;
                    }
                    return;
                }
            } catch (IOException e) {
                log.error("构建合并信息文件时出现IOException: ", e);
                return;
            }
        }

        //3. 生成DASH文件
        List<String> concatFiles = qualityVideoFilesMap.values().stream().map(File::getAbsolutePath).toList();
        List<String> audioFiles = audioEncodeTasks.stream().map(AudioEncodeTask::getResultAudioUrl).toList();
        try {
            File dashRootDir = new File(System.getProperty("java.io.tmpdir") + "/dash-" + concatRequestDTO.getVideoId());
            boolean ignored = dashRootDir.mkdirs();
            //DASH描述文件的存放位置

            File dashMpdFile = new File(dashRootDir, "representations.mpd");
            //DASH不同质量的媒体流名称的模板
            String dashFileNameTemplate = "dash-seg-%d-$RepresentationID$.$ext$".formatted(concatRequestDTO.getVideoId());
            MediaUtils.dashGenerate(concatFiles, audioFiles, dashMpdFile, dashFileNameTemplate);
            //上传
            try {
                minioClient.uploadObject(
                        UploadObjectArgs.builder()
                                .bucket(s3Config.getBucket())
                                .object("video/dash/%d/%s".formatted(concatRequestDTO.getVideoId(), dashMpdFile.getName()))
                                .filename(dashMpdFile.getAbsolutePath())
                                .contentType("application/dash+xml")
                                .build()
                );

                for (int i = 0; i < concatFiles.size() + audioFiles.size(); i++) {
                    String streamFileName = "dash-seg-%d-%d.mp4".formatted(concatRequestDTO.getVideoId(), i);
                    minioClient.uploadObject(
                            UploadObjectArgs.builder()
                                    .bucket(s3Config.getBucket())
                                    .object("video/dash/%d/%s".formatted(concatRequestDTO.getVideoId(), streamFileName))
                                    .filename(dashMpdFile.getParent() + "/" + streamFileName)
                                    .contentType("video/mp4")
                                    .build()
                    );
                }

                videoFeignClient.updateVideoResourceConversionState(new UpdateVideoResourceConversionStateDTO(
                        concatRequestDTO.getVideoId(),
                        3,
                        null,
                        null,
                        String.format("%s/%s/video/dash/%d/%s", s3Config.getEndpoint(), s3Config.getBucket(), concatRequestDTO.getVideoId(), dashMpdFile.getName()),
                        null,
                        "",
                        ""
                ));
            } catch (Exception e) {
                log.error("在上传DASH内容时发生异常：", e);
                return;
            }
        } catch (IOException e) {
            log.error("生成DASH描述文件时出现IOException: ", e);
            return;
        } catch (InterruptedException interruptedException) {
            log.error("生成DASH视频时被中断：", interruptedException);
            return;
        }

        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            log.error("消息ack时发生IOException: ", e);
            return;
        }
    }
}
