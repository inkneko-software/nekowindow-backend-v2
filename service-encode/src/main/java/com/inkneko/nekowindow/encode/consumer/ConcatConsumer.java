package com.inkneko.nekowindow.encode.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkneko.nekowindow.api.video.client.VideoFeignClient;
import com.inkneko.nekowindow.api.video.dto.UpdateVideoResourceConversionStateDTO;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.common.util.OssUtils;
import com.inkneko.nekowindow.encode.config.EncodeConfig;
import com.inkneko.nekowindow.encode.config.S3Config;
import com.inkneko.nekowindow.encode.dto.ConcatRequestDTO;
import com.inkneko.nekowindow.encode.entity.AudioEncodeTask;
import com.inkneko.nekowindow.encode.entity.VideoEncodeTask;
import com.inkneko.nekowindow.encode.service.EncodeService;
import com.inkneko.nekowindow.encode.util.MediaUtils;
import com.rabbitmq.client.Channel;
import io.minio.DownloadObjectArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
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

        //更新转码状态为正在合并
        videoFeignClient.updateVideoResourceConversionState(new UpdateVideoResourceConversionStateDTO(
                concatRequestDTO.getVideoId(),
                2,
                null,
                null,
                null,
                null,
                null,
                null
        ));

        log.info("收到合并请求：{}", concatRequestDTO);
        //即将合并的临时文件的根目录
        File dashRootDir = new File(System.getProperty("java.io.tmpdir") + "/dash-" + concatRequestDTO.getVideoId());
        boolean ignored = dashRootDir.mkdirs();


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
                File inputVideosTxtFile = new File(dashRootDir, "concat_info-%d-%d-".formatted(concatRequestDTO.getVideoId(), qualityCode) + ".txt");
                try (FileWriter fileWriter = new FileWriter(inputVideosTxtFile)) {
                    StringBuilder stringBuilder = new StringBuilder();

                    for (VideoEncodeTask videoEncodeTask : qualityVideoEncodeTaskList) {
                        OssUtils.OssLink ossLink = OssUtils.url(videoEncodeTask.getResultVideoUrl());
                        String objectKey = ossLink.key;
                        String filename = objectKey.substring(objectKey.lastIndexOf("/") + 1, objectKey.lastIndexOf("."));
                        String ext = objectKey.substring(objectKey.lastIndexOf("."));

                        File videoSegmentFile = new File(dashRootDir, filename + ext);
                        //不能覆盖已有的文件
                        videoSegmentFile.delete();
                        try {
                            minioClient.downloadObject(DownloadObjectArgs.builder().bucket(s3Config.getBucket()).object(objectKey).filename(videoSegmentFile.getAbsolutePath()).build());
                        } catch (Exception e) {
                            log.error("下载分片文件时出现错误：", e);
                            return;
                        }
                        //ffmpeg -f concat 只允许文件名，不能包含目录
                        stringBuilder.append("file %s%s\n".formatted(filename, ext));
                    }
                    fileWriter.write(stringBuilder.toString());
                }
                File outputFile = new File(dashRootDir, "%d-%d-".formatted(concatRequestDTO.getVideoId(), qualityCode) + ".mp4");
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
        List<String> concatFiles = qualityVideoFilesMap.keySet().stream().sorted().map(qualityCode -> qualityVideoFilesMap.get(qualityCode).getAbsolutePath()).toList();
        List<String> audioFiles = new ArrayList<>();
        for (AudioEncodeTask audioEncodeTask : audioEncodeTasks.stream().sorted(Comparator.comparingInt(AudioEncodeTask::getAudioQualityCode)).toList()) {
            try {
                OssUtils.OssLink ossLink = OssUtils.url(audioEncodeTask.getResultAudioUrl());
                String objectKey = ossLink.key;
                String filename = objectKey.substring(objectKey.lastIndexOf("/") + 1, objectKey.lastIndexOf("."));
                String ext = objectKey.substring(objectKey.lastIndexOf("."));
                File audioFile = new File(dashRootDir, filename + ext);
                audioFile.delete();

                minioClient.downloadObject(DownloadObjectArgs.builder().bucket(s3Config.getBucket()).object(objectKey).filename(audioFile.getAbsolutePath()).build());
                audioFiles.add(audioFile.getAbsolutePath());
            } catch (Exception e) {
                log.error("下载音频流时出现错误：", e);
                return;
            }
        }
        try {

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

                log.info("适配代码列表 {}", String.join(",", audioEncodeTasks.stream().sorted().map(Object::toString).toList()));

                videoFeignClient.updateVideoResourceConversionState(new UpdateVideoResourceConversionStateDTO(
                        concatRequestDTO.getVideoId(),
                        3,
                        null,
                        null,
                        String.format("%s/%s/video/dash/%d/%s", s3Config.getEndpoint(), s3Config.getBucket(), concatRequestDTO.getVideoId(), dashMpdFile.getName()),
                        null,
                        String.join(",", qualityVideoFilesMap.keySet().stream().sorted().map(Object::toString).toList()),
                        String.join(",", audioEncodeTasks.stream().sorted().map(task->task.getAudioQualityCode().toString()).toList())
                ));

                dashRootDir.delete();

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
        }
    }
}
