package com.inkneko.nekowindow.encode.producer;

import com.inkneko.nekowindow.encode.config.EncodeConfig;
import com.inkneko.nekowindow.encode.dto.ConcatRequestDTO;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ConcatProducer {
    RabbitTemplate rabbitTemplate;

    public ConcatProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void produceConcatMessage(Long videoId) {
        rabbitTemplate.convertAndSend(EncodeConfig.NK_TOPIC_EXCHANGE_NAME, "concat", new ConcatRequestDTO(videoId));
    }
}
