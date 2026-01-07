package com.inkneko.nekowindow.video.service;

import com.inkneko.nekowindow.api.video.dto.VideoPostDTO;

import java.util.List;
import java.util.Map;

public interface VideoInternalService {
    /**
     * 批量获取视频投稿简略信息
     *
     * @param nkidList     稿件ID列表
     * @param viewerUserId 访问者用户ID
     * @return 稿件列表，若查询的nkid对当前用户不可见，则不包含在结果中
     */
    Map<Long, VideoPostDTO> getVideoPostBatch(List<Long> nkidList, Long viewerUserId);

    /**
     * 获取单个视频投稿简略信息
     *
     * @param nkid         稿件ID
     * @param viewerUserId 访问者用户ID
     * @return 稿件信息，若查询的nkid对当前用户不可见，则返回null
     */
    VideoPostDTO getVideoPost(Long nkid, Long viewerUserId);
}
