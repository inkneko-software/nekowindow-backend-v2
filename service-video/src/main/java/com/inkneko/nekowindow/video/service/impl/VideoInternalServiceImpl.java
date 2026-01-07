package com.inkneko.nekowindow.video.service.impl;

import com.inkneko.nekowindow.api.video.dto.VideoPostDTO;
import com.inkneko.nekowindow.video.entity.VideoPost;
import com.inkneko.nekowindow.video.mapper.VideoPostMapper;
import com.inkneko.nekowindow.video.permission.policy.VideoPostVisibilityPolicy;
import com.inkneko.nekowindow.video.permission.scene.VideoPostAccessScene;
import com.inkneko.nekowindow.video.service.VideoInternalService;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VideoInternalServiceImpl implements VideoInternalService {

    VideoPostMapper videoPostMapper;

    public VideoInternalServiceImpl(VideoPostMapper videoPostMapper) {
        this.videoPostMapper = videoPostMapper;
    }

    @Override
    public Map<Long, VideoPostDTO> getVideoPostBatch(List<Long> nkidList, Long viewerUserId) {
        List<VideoPost> videoPosts = videoPostMapper.selectBatchIds(nkidList);
        Map<Long, VideoPostDTO> result = new HashMap<>();
        for (VideoPost videoPost : videoPosts) {
            VideoPostAccessScene scene = VideoPostAccessScene.PUBLIC;
            if (viewerUserId != null && viewerUserId.equals(videoPost.getUid())) {
                scene = VideoPostAccessScene.OWNER;
            }

            if (VideoPostVisibilityPolicy.visibleStates(scene).contains(videoPost.getState())) {
                VideoPostDTO dto = new VideoPostDTO();
                dto.setNkid(videoPost.getNkid());
                dto.setUid(videoPost.getUid());
                dto.setTitle(videoPost.getTitle());
                dto.setCoverUrl(videoPost.getCoverUrl());
                dto.setDuration(videoPost.getDuration());
                dto.setShared(videoPost.getShared());
                dto.setDescription(videoPost.getDescription());
                dto.setPartitionId(videoPost.getPartitionId());
                dto.setPartitionName(videoPost.getPartitionName());
                dto.setCreatedAt(videoPost.getCreatedAt());

                result.put(videoPost.getNkid(), dto);
            }
        }
        return result;
    }

    @Override
    public VideoPostDTO getVideoPost(Long nkid, Long viewerUserId) {
        VideoPost videoPost = videoPostMapper.selectById(nkid);
        if (videoPost == null) {
            return null;
        }

        VideoPostAccessScene scene = VideoPostAccessScene.PUBLIC;
        if (viewerUserId != null && viewerUserId.equals(videoPost.getUid())) {
            scene = VideoPostAccessScene.OWNER;
        }

        if (!VideoPostVisibilityPolicy.visibleStates(scene).contains(videoPost.getState())) {
            return null;
        }

        VideoPostDTO dto = new VideoPostDTO();
        dto.setNkid(videoPost.getNkid());
        dto.setUid(videoPost.getUid());
        dto.setTitle(videoPost.getTitle());
        dto.setCoverUrl(videoPost.getCoverUrl());
        dto.setDuration(videoPost.getDuration());
        dto.setShared(videoPost.getShared());
        dto.setDescription(videoPost.getDescription());
        dto.setPartitionId(videoPost.getPartitionId());
        dto.setPartitionName(videoPost.getPartitionName());
        dto.setCreatedAt(videoPost.getCreatedAt());

        return dto;
    }
}
