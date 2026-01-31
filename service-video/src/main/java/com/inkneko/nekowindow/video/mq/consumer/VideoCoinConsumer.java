package com.inkneko.nekowindow.video.mq.consumer;

import com.inkneko.nekowindow.api.mq.PostVideoCoinDTO;
import com.inkneko.nekowindow.video.entity.VideoCoinRecord;
import com.inkneko.nekowindow.video.mapper.VideoCoinRecordMapper;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(
        topic = "video-coin-topic",
        consumerGroup = "video-coin-tx-producer-group"
)
public class VideoCoinConsumer implements RocketMQListener<PostVideoCoinDTO> {

    private final VideoCoinRecordMapper mapper;

    public VideoCoinConsumer(VideoCoinRecordMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void onMessage(PostVideoCoinDTO dto) {
        VideoCoinRecord record = new VideoCoinRecord();
        record.setUid(dto.getUserId());
        record.setNkid(dto.getNkid());
        record.setNum(dto.getCoinNum());
        record.setOrderId(dto.getOrderId());
        mapper.insert(record);
    }
}