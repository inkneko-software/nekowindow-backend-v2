package com.inkneko.nekowindow.video.mq.producer;

import com.inkneko.nekowindow.api.mq.video.VideoPostCreatedDTO;
import com.inkneko.nekowindow.video.entity.VideoPost;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.stereotype.Component;

@Component
public class VideoPostCreatedProducer {
    RocketMQTemplate rocketMQTemplate;
    public VideoPostCreatedProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }
    public void send(VideoPost videoPost) {
        rocketMQTemplate.convertAndSend("video-post-created-topic",
                new VideoPostCreatedDTO(
                        videoPost.getNkid(),
                        videoPost.getUid(),
                        videoPost.getTitle(),
                        videoPost.getDescription(),
                        videoPost.getCoverUrl(),
                        videoPost.getCreatedAt()
                ));
    }
}
