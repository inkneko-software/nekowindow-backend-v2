package com.inkneko.nekowindow.encode.rabbitmq;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.encode.config.EncodeConfig;
import com.inkneko.nekowindow.encode.entity.EncodeRequest;
import com.inkneko.nekowindow.encode.service.EncodeService;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class EncodeConsume {
    Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    EncodeService encodeService;

    @RabbitListener(queues = EncodeConfig.encodeQueueName, ackMode = "MANUAL")
    public void probe(Channel channel, Message message) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            EncodeRequest encodeRequest = objectMapper.readValue(message.getBody(), EncodeRequest.class);
            encodeService.encodeVideo(encodeRequest.getSourceVideoUrl());
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        }catch (IOException e){
            logger.error("转码错误", e);
        }catch (ServiceException e){
            logger.error("");
        }
    }
}
