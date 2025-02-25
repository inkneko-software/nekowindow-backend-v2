package com.inkneko.nekowindow.encode.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.encode.config.EncodeConfig;
import com.inkneko.nekowindow.encode.config.S3Config;
import com.inkneko.nekowindow.encode.dto.AudioEncodeDTO;
import com.inkneko.nekowindow.encode.dto.VideoSegmentEncodeDTO;
import com.inkneko.nekowindow.encode.producer.ConcatProducer;
import com.inkneko.nekowindow.encode.service.EncodeService;
import com.inkneko.nekowindow.encode.util.MediaUtils;
import com.rabbitmq.client.Channel;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * 转码消息消费者
 */
@Component
@Slf4j
public class EncodeConsumer {
    EncodeService encodeService;
    RabbitTemplate rabbitTemplate;
    MinioClient minioClient;
    RedissonClient redissonClient;
    ConcatProducer concatProducer;
    S3Config s3Config;

    public EncodeConsumer(EncodeService encodeService,
                          RabbitTemplate rabbitTemplate,
                          MinioClient minioClient,
                          RedissonClient redissonClient,
                          ConcatProducer concatProducer,
                          S3Config s3Config) {
        this.encodeService = encodeService;
        this.rabbitTemplate = rabbitTemplate;
        this.minioClient = minioClient;
        this.redissonClient = redissonClient;
        this.concatProducer = concatProducer;
        this.s3Config = s3Config;
    }

    @RabbitListener(queues = EncodeConfig.NK_ENCODE_VIDEO_QUEUE_NAME, ackMode = "MANUAL")
    public void videoEncodeConsumer(Channel channel, Message message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            VideoSegmentEncodeDTO videoSegmentEncodeDTO = objectMapper.readValue(message.getBody(), VideoSegmentEncodeDTO.class);
            log.info("执行视频转码, 参数: {}", videoSegmentEncodeDTO.toString());
            File tmpOutputFile = File.createTempFile(
                    String.format(
                            "%d-%d-%s-%s-%d-%d-",
                            videoSegmentEncodeDTO.getVideoId(),
                            System.currentTimeMillis(),
                            videoSegmentEncodeDTO.getTargetCodec(),
                            videoSegmentEncodeDTO.getTargetBitrate(),
                            videoSegmentEncodeDTO.getCurrentSegment(),
                            videoSegmentEncodeDTO.getVideoQualityCode()
                    ),
                    ".mp4"
            );

            try {
                log.info("ss起始：{}, currentSegment: {}, segmentDuration: {}",
                        (videoSegmentEncodeDTO.getCurrentSegment() - 1) * videoSegmentEncodeDTO.getSegmentDuration(),
                        videoSegmentEncodeDTO.getCurrentSegment(),
                        videoSegmentEncodeDTO.getSegmentDuration()
                );

                MediaUtils.encodeVideo(
                        videoSegmentEncodeDTO.getSourceVideoURL(),
                        tmpOutputFile,
                        //为了消除BigDecimal.value(float)产生的误差
                        BigDecimal.valueOf(videoSegmentEncodeDTO.getCurrentSegment() - 1).multiply(new BigDecimal(videoSegmentEncodeDTO.getSegmentDuration().toString())).toString(),
                        videoSegmentEncodeDTO.getSegmentDuration().toString(),
                        videoSegmentEncodeDTO.getTargetCodec(),
                        videoSegmentEncodeDTO.getTargetBitrate(),
                        videoSegmentEncodeDTO.getTargetFrameRate(),
                        videoSegmentEncodeDTO.getTargetGopSize(),
                        videoSegmentEncodeDTO.getScale()
                );

                String resultVideoUrl = "encode/video_segments/%s".formatted(tmpOutputFile.getName());
                minioClient.uploadObject(
                        UploadObjectArgs.builder()
                                .bucket(s3Config.getBucket())
                                .object(resultVideoUrl)
                                .filename(tmpOutputFile.getAbsolutePath())
                                .contentType("video/mp4")
                                .build()
                );

                RLock taskLock = redissonClient.getLock(
                        String.format("EncodeService.CheckVideoConcatTaskLock.%d", videoSegmentEncodeDTO.getVideoId())
                );
                try {
                    taskLock.lock(1, TimeUnit.MINUTES);
                    //设置编码任务完成
                    encodeService.updateVideoEncodeTaskComplete(
                            videoSegmentEncodeDTO.getVideoId(),
                            videoSegmentEncodeDTO.getCurrentSegment(),
                            videoSegmentEncodeDTO.getVideoQualityCode(),
                            "%s/%s/%s".formatted(s3Config.getEndpoint(), s3Config.getBucket(), resultVideoUrl)
                    );

                    //检查是否全部完成
                    if (encodeService.isVideoEncodeTaskCompleted(videoSegmentEncodeDTO.getVideoId())) {
                        //发送合并消息
                        concatProducer.produceConcatMessage(videoSegmentEncodeDTO.getVideoId());
                    }
                } finally {
                    taskLock.unlock();
                }
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

            } catch (ServiceException e) {
                log.error("视频转码错误", e);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (InterruptedException e) {
                log.error("视频转码进程被中断");
            } catch (IOException e) {
                log.error("视频转码过程中出现IO错误", e);
            }
            //boolean ignored = tmpOutputFile.delete();

        } catch (Exception e) {
            log.error("创建目标文件时出现错误", e);
        }
    }

    @RabbitListener(queues = EncodeConfig.NK_ENCODE_AUDIO_QUEUE_NAME, ackMode = "MANUAL")
    public void audioEncodeConsumer(Channel channel, Message message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            AudioEncodeDTO audioEncodeDTO = objectMapper.readValue(message.getBody(), AudioEncodeDTO.class);
            log.info("执行音频转码, 参数: {}", audioEncodeDTO.toString());
            File tmpOutputFile = File.createTempFile(
                    String.format(
                            "%d-%d-%s-%s-%d-",
                            audioEncodeDTO.getVideoId(),
                            System.currentTimeMillis(),
                            audioEncodeDTO.getTargetCodec(),
                            audioEncodeDTO.getTargetBitrate(),
                            audioEncodeDTO.getAudioQualityCode()
                    ),
                    ".m4a"
            );
            try {
                MediaUtils.encodeAudio(
                        audioEncodeDTO.getSourceVideoURL(),
                        tmpOutputFile,
                        audioEncodeDTO.getTargetCodec(),
                        audioEncodeDTO.getTargetBitrate()
                );

                String resultAudioUrl = "encode/audio/%s".formatted(tmpOutputFile.getName());
                minioClient.uploadObject(
                        UploadObjectArgs.builder()
                                .bucket(s3Config.getBucket())
                                .object(resultAudioUrl)
                                .filename(tmpOutputFile.getAbsolutePath())
                                .contentType("audio/mp4")
                                .build()
                );

                RLock taskLock = redissonClient.getLock(
                        String.format("EncodeService.CheckVideoConcatTaskLock.%d", audioEncodeDTO.getVideoId())
                );
                try {
                    taskLock.lock(1, TimeUnit.MINUTES);
                    //设置编码任务完成
                    encodeService.updateAudioEncodeTaskComplete(
                            audioEncodeDTO.getVideoId(),
                            audioEncodeDTO.getAudioQualityCode(),
                            "%s/%s/%s".formatted(s3Config.getEndpoint(), s3Config.getBucket(), resultAudioUrl)
                    );

                    //检查是否全部完成
                    if (encodeService.isVideoEncodeTaskCompleted(audioEncodeDTO.getVideoId())) {
                        //发送合并消息
                        concatProducer.produceConcatMessage(audioEncodeDTO.getVideoId());
                    }
                } finally {
                    taskLock.unlock();
                }
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

            } catch (ServiceException e) {
                log.error("音频转码错误", e);
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } catch (InterruptedException e) {
                log.error("音频转码进程被中断");
            } catch (IOException e) {
                log.error("音频转码过程中出现IO错误", e);
            }
        } catch (Exception e) {
            log.error("创建目标文件时出现错误", e);
        }

    }


}
