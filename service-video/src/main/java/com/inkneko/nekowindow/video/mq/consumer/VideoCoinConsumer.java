package com.inkneko.nekowindow.video.mq.consumer;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.inkneko.nekowindow.api.mq.PostVideoCoinDTO;
import com.inkneko.nekowindow.video.entity.VideoCoinRecord;
import com.inkneko.nekowindow.video.entity.VideoPost;
import com.inkneko.nekowindow.video.mapper.VideoCoinRecordMapper;
import com.inkneko.nekowindow.video.mapper.VideoPostMapper;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RocketMQMessageListener(
        topic = "video-coin-topic",
        consumerGroup = "video-coin-tx-producer-group"
)
public class VideoCoinConsumer implements RocketMQListener<PostVideoCoinDTO> {

    private final VideoCoinRecordMapper mapper;
    private final VideoPostMapper videoPostMapper;

    public VideoCoinConsumer(
            VideoCoinRecordMapper mapper,
            VideoPostMapper videoPostMapper
            ) {
        this.mapper = mapper;
        this.videoPostMapper = videoPostMapper;
    }

    @Override
    @Transactional
    public void onMessage(PostVideoCoinDTO dto) {
        VideoCoinRecord record = new VideoCoinRecord();
        record.setUid(dto.getUserId());
        record.setNkid(dto.getNkid());
        record.setNum(dto.getCoinNum());
        record.setOrderId(dto.getOrderId());
        mapper.insert(record);

        videoPostMapper.update(
                null,
                Wrappers.<VideoPost>lambdaUpdate()
                        .eq(VideoPost::getNkid, dto.getNkid())
                        .setSql("coin = coin + " + dto.getCoinNum())
        );
    }
}