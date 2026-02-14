package com.inkneko.nekowindow.mp.consumer;

import com.inkneko.nekowindow.api.activity.dto.VideoActivityDTO;
import com.inkneko.nekowindow.api.mq.video.VideoPostCreatedDTO;
import com.inkneko.nekowindow.service.ActivityService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = "video-post-created-topic",
        consumerGroup = "activity-consumer-group"
)
public class VideoPostCreatedConsumer implements RocketMQListener<VideoPostCreatedDTO> {
    private final ActivityService activityService;

    public VideoPostCreatedConsumer(ActivityService activityService) {
        this.activityService = activityService;
    }
    @Override
    public void onMessage(VideoPostCreatedDTO dto) {
        activityService.saveVideoPostActivity(dto.getNkid(), dto.getUserId());
    }
}
