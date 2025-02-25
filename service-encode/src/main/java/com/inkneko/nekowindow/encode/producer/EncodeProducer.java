package com.inkneko.nekowindow.encode.producer;

import com.inkneko.nekowindow.encode.config.EncodeConfig;
import com.inkneko.nekowindow.encode.dto.AudioEncodeDTO;
import com.inkneko.nekowindow.encode.dto.ProbeRequestDTO;
import com.inkneko.nekowindow.encode.dto.VideoSegmentEncodeDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class EncodeProducer {
    RabbitTemplate rabbitTemplate;
    public EncodeProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 生成某视频的分片编码任务消息
     *
     * @param dto 请求参数
     */
    public void produceVideoSegmentEncodeMessage(VideoSegmentEncodeDTO dto){
        rabbitTemplate.convertAndSend(EncodeConfig.NK_TOPIC_EXCHANGE_NAME, "encode.video", dto);

    }

    /**
     * 生成音频的编码任务消息
     *
     * @param dto 请求参数
     */
    public void produceAudioEncodeMessage(AudioEncodeDTO dto){
        rabbitTemplate.convertAndSend(EncodeConfig.NK_TOPIC_EXCHANGE_NAME, "encode.audio", dto);

    }


}
