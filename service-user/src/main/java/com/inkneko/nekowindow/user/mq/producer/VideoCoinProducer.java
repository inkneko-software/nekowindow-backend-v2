package com.inkneko.nekowindow.user.mq.producer;

import com.inkneko.nekowindow.api.mq.PostVideoCoinDTO;
import jakarta.annotation.Resource;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.client.producer.TransactionSendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class VideoCoinProducer {
    @Resource(name = "videoCoinExtRocketMQTemplate")
    RocketMQTemplate videoCoinExtRocketMQTemplate;
    public boolean sendCoin(Long orderId, Long userId, Long nkid, Integer coinNum) {
        PostVideoCoinDTO dto = new PostVideoCoinDTO(
                orderId,
                nkid,
                userId,
                coinNum
        );
        TransactionSendResult result = videoCoinExtRocketMQTemplate.sendMessageInTransaction(
            "video-coin-topic",
            MessageBuilder.withPayload(dto).build(),
            orderId
        );
        return result.getSendStatus().equals(SendStatus.SEND_OK);
    }
}
