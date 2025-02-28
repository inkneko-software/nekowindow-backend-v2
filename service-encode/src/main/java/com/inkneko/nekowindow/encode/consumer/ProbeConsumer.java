package com.inkneko.nekowindow.encode.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkneko.nekowindow.api.video.client.VideoFeignClient;
import com.inkneko.nekowindow.api.video.dto.UpdateVideoResourceConversionStateDTO;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.encode.config.EncodeConfig;
import com.inkneko.nekowindow.encode.dto.AudioEncodeDTO;
import com.inkneko.nekowindow.encode.dto.VideoSegmentEncodeDTO;
import com.inkneko.nekowindow.encode.dto.ProbeRequestDTO;
import com.inkneko.nekowindow.encode.entity.*;
import com.inkneko.nekowindow.encode.producer.EncodeProducer;
import com.inkneko.nekowindow.encode.service.EncodeService;
import com.rabbitmq.client.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class ProbeConsumer {

    private final EncodeConfig encodeConfig;
    EncodeService encodeService;
    RabbitTemplate rabbitTemplate;
    VideoFeignClient videoFeignClient;
    EncodeProducer encodeProducer;

    public ProbeConsumer(EncodeService encodeService,
                         RabbitTemplate rabbitTemplate,
                         VideoFeignClient videoFeignClient,
                         EncodeProducer encodeProducer, EncodeConfig encodeConfig) {
        this.encodeService = encodeService;
        this.rabbitTemplate = rabbitTemplate;
        this.videoFeignClient = videoFeignClient;
        this.encodeProducer = encodeProducer;
        this.encodeConfig = encodeConfig;
    }

    @RabbitListener(queues = EncodeConfig.NK_PROBE_QUEUE_NAME, ackMode = "MANUAL")
    public void probe(Channel channel, Message message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            ProbeRequestDTO probeRequestDTO = objectMapper.readValue(message.getBody(), ProbeRequestDTO.class);
            log.info("收到视频分析请求: {}", probeRequestDTO);
            ProbeResult result = encodeService.probeVideo(probeRequestDTO);

            log.info("分析结果: {}", result.toString());
            /*
             * 第一步，获取视频/音频流，只使用第一个流
             */
            ProbeResult.Stream videoStream = null;
            ProbeResult.Stream audioStream = null;

            if (!result.getStreams().isEmpty()) {
                //只使用第一个视频流与音频流
                for (ProbeResult.Stream stream : result.getStreams()) {
                    if (videoStream == null && stream.getCodecType().compareTo("video") == 0) {
                        videoStream = stream;
                    }
                    if (audioStream == null && stream.getCodecType().compareTo("audio") == 0) {
                        audioStream = stream;
                    }
                }
            }

            if (videoStream == null) {
                videoFeignClient.updateVideoResourceConversionState(new UpdateVideoResourceConversionStateDTO(
                        probeRequestDTO.getVideoId(),
                        4,
                        "提交视频没有视频流，请检查文件",
                        null,
                        null,
                        null,
                        null,
                        null
                ));
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            /*
             * 第二步，获取帧率，用于判断是否生成60帧视频。
             *
             * 帧率格式有两种，整数型（如24），或者NTSC格式（如24000/1001）
             *
             * 2025-02-26：目前看来还有两种，23.967, 24/1
             *
             * //TODO: 处理23.967（24000/1001）格式
             */
            String frameRateString = videoStream.getRFrameRate();
            Integer frameRate = null;
            Pattern ratioFrameRatePattern = Pattern.compile("(\\d+)/1001");
            Pattern numericFrameRatePattern = Pattern.compile("(\\d+)(/1)*");

            Matcher ratioMatcher = ratioFrameRatePattern.matcher(frameRateString);
            Matcher numericFrameRateMatcher = numericFrameRatePattern.matcher(frameRateString);
            if (ratioMatcher.matches()) {
                int tmp = Integer.parseInt(ratioMatcher.group(1));
                if (tmp > 1001) {
                    frameRate = tmp / 1000;
                }
            } else if (numericFrameRateMatcher.matches()) {
                frameRate = Integer.parseInt(numericFrameRateMatcher.group(1));
            }

            if (frameRate == null) {
                videoFeignClient.updateVideoResourceConversionState(new UpdateVideoResourceConversionStateDTO(
                        probeRequestDTO.getVideoId(),
                        4,
                        "提交视频帧率错误，请检查文件。如认为误报，请通知后台处理",
                        null,
                        null,
                        null,
                        null,
                        null
                ));
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            /*
             * 第三步，根据视频分辨率，生成不同分辨率的视频
             *
             * 三档分辨率 1080P 720P 360P，
             *
             * 其中1080P支持60帧, 若帧数大于60则生成30帧与60帧视频，最大码率分别为3Mb和8Mb。若不大于60帧但码率超过3Mb，则生成最大码率为3Mb和8Mb的视频，帧数不变
             *
             * 720P最大码率1M
             *
             * 360P最大码率500Kbps
             *
             */

            int videoHeight = videoStream.getHeight();
            int videoBitrate = Integer.parseInt(result.getFormat().getBitRate());
            List<VideoEncodeParameter> videoAdaptionList = new ArrayList<>();
            if (videoHeight >= 1080) {
                if (frameRate >= 60) {
                    //如果帧率大于60,则生成60帧与30帧
                    VideoEncodeParameter parameter1080P60 = new VideoEncodeParameter(
                            "8M",
                            VideoEncodeTask.QUALITY_1080P_60,
                            "h264",
                            ratioMatcher.matches() ? "60000/1001" : "60",
                            "20",
                            encodeConfig.getGopSize(),
                            "-1:1080",
                            1080
                    );
                    VideoEncodeParameter parameter1080P = new VideoEncodeParameter(
                            "3M",
                            VideoEncodeTask.QUALITY_1080P,
                            "h264",
                            ratioMatcher.matches() ? "30000/1001" : "30",
                            "20",
                            encodeConfig.getGopSize(),
                            "-1:1080",
                            1080
                    );
                    videoAdaptionList.add(parameter1080P60);
                    videoAdaptionList.add(parameter1080P);

                } else if (videoBitrate > 3 * 1000 * 1000) {
                    //否则根据码率生成高码率与普通码率视频
                    VideoEncodeParameter parameter1080PHD = new VideoEncodeParameter(
                            "8M",
                            VideoEncodeTask.QUALITY_1080P_HQ,
                            "h264",
                            frameRateString,
                            "20",
                            encodeConfig.getGopSize(),
                            "-1:1080",
                            1080
                    );
                    VideoEncodeParameter parameter1080P = new VideoEncodeParameter(
                            "3M",
                            VideoEncodeTask.QUALITY_1080P,
                            "h264",
                            frameRateString,
                            "20",
                            encodeConfig.getGopSize(),
                            "-1:1080",
                            1080
                    );
                    videoAdaptionList.add(parameter1080PHD);
                    videoAdaptionList.add(parameter1080P);
                } else {
                    VideoEncodeParameter parameter1080P = new VideoEncodeParameter(
                            "3M",
                            VideoEncodeTask.QUALITY_1080P,
                            "h264",
                            frameRateString,
                            "20",
                            encodeConfig.getGopSize(),
                            "-1:1080",
                            1080
                    );
                    videoAdaptionList.add(parameter1080P);

                }

            }

            if (videoStream.getHeight() >= 720) {
                VideoEncodeParameter parameter720 = new VideoEncodeParameter(
                        "1M",
                        VideoEncodeTask.QUALITY_720P,
                        "h264",
                        frameRateString,
                        "20",
                        encodeConfig.getGopSize(),
                        "-1:720",
                        720
                );
                videoAdaptionList.add(parameter720);
            }

            if (videoStream.getHeight() >= 360) {
                VideoEncodeParameter parameter360 = new VideoEncodeParameter(
                        "500k",
                        VideoEncodeTask.QUALITY_360P,
                        "h264",
                        frameRateString,
                        "20",
                        encodeConfig.getGopSize(),
                        "-1:360",
                        360
                );
                videoAdaptionList.add(parameter360);
            } else {
                VideoEncodeParameter parameterFallback = new VideoEncodeParameter(
                        "500k",
                        VideoEncodeTask.QUALITY_360P,
                        "h264",
                        frameRateString,
                        "20",
                        encodeConfig.getGopSize(),
                        null,
                        360
                );
                videoAdaptionList.add(parameterFallback);
            }
            /*
             * 第四步，生成音频，两档，最大128kbps与320kbps
             */
            List<AudioEncodeParameter> audioAdaptionList = new ArrayList<>();
            if (audioStream != null) {
                int audioBitrate =  audioStream.getBitRate() != null ? Integer.parseInt(audioStream.getBitRate()) : 0;
                if (audioBitrate >= 320 * 1000) {
                    audioAdaptionList.add(new AudioEncodeParameter(
                            "320k",
                            AudioEncodeTask.QUALITY_320K,
                            "aac"
                    ));
                }
                audioAdaptionList.add(new AudioEncodeParameter(
                        "128k",
                        AudioEncodeTask.QUALITY_128K,
                        "aac"
                ));

            }

            log.info("视频编码适配列表：{}", videoAdaptionList);
            log.info("音频编码适配列表：{}", audioAdaptionList);

            /*
             * 第五步，为每个品质档次，生成分片编码消息，并同步至数据库
             */
            float duration = Float.parseFloat(result.getFormat().getDuration());
            float segmentDuration = ratioMatcher.matches() ? 120f * 1.001f : 120f;
            int segmentTotal = (int)Math.ceil(duration / segmentDuration);

            log.info("分片数：{}, segmentDuration: {}, totalDuration: {}", segmentTotal, segmentDuration, duration);

            for (VideoEncodeParameter videoEncodeParameter : videoAdaptionList) {
                for (int i = 1; i <= segmentTotal; ++i) {

                    VideoSegmentEncodeDTO videoSegmentEncodeDTO = new VideoSegmentEncodeDTO(
                            probeRequestDTO.getVideoId(),
                            videoEncodeParameter.getVideoQualityCode(),
                            probeRequestDTO.getSourceVideoUrl(),
                            i,
                            segmentTotal,
                            segmentDuration,
                            duration,
                            videoEncodeParameter.getVideoScaleOption(),
                            videoEncodeParameter.getMaxBitRate(),
                            videoEncodeParameter.getCodec(),
                            videoEncodeParameter.getFrameRate(),
                            videoEncodeParameter.getVideoGopSize(),
                            videoEncodeParameter.getHeight()
                    );
                    encodeService.saveVideoEncodeTask(new VideoEncodeTask(videoSegmentEncodeDTO));

                    encodeProducer.produceVideoSegmentEncodeMessage(videoSegmentEncodeDTO);
                }

            }

            for (AudioEncodeParameter audioEncodeParameter : audioAdaptionList) {
                AudioEncodeDTO audioEncodeDTO = new AudioEncodeDTO(
                        probeRequestDTO.getVideoId(),
                        audioEncodeParameter.getAudioQualityCode(),
                        probeRequestDTO.getSourceVideoUrl(),
                        audioEncodeParameter.getCodec(),
                        audioEncodeParameter.getMaxBitRate()
                );
                encodeService.saveAudioEncodeTask(new AudioEncodeTask(audioEncodeDTO));
                encodeProducer.produceAudioEncodeMessage(audioEncodeDTO);
            }

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            log.error("probe错误", e);
        } catch (ServiceException e) {
            log.error("");
        } catch (InterruptedException e) {
            log.error("interrupted", e);
        }
    }


}
