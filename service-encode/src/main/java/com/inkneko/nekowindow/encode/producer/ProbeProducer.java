package com.inkneko.nekowindow.encode.producer;

import com.inkneko.nekowindow.encode.config.EncodeConfig;
import com.inkneko.nekowindow.encode.dto.ProbeRequestDTO;
import org.checkerframework.checker.units.qual.A;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ProbeProducer {
    RabbitTemplate rabbitTemplate;

    public ProbeProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void produceProbeMessage(Long videoId, String sourceVideoUrl) {
        rabbitTemplate.convertAndSend(EncodeConfig.NK_TOPIC_EXCHANGE_NAME, "probe", new ProbeRequestDTO(videoId, sourceVideoUrl));
    }


}
