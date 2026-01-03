package com.inkneko.nekowindow.video.service;

import com.inkneko.nekowindow.api.video.dto.UpdateVideoResourceConversionStateDTO;
import com.inkneko.nekowindow.video.dto.CreateVideoPostDTO;
import com.inkneko.nekowindow.video.dto.UpdatePostBriefDTO;
import com.inkneko.nekowindow.video.entity.PartitionInfo;
import com.inkneko.nekowindow.video.entity.PartitionRecommendTag;
import com.inkneko.nekowindow.video.entity.VideoPost;
import com.inkneko.nekowindow.video.entity.VideoPostResource;
import com.inkneko.nekowindow.video.vo.CreateVideoPostVO;
import com.inkneko.nekowindow.video.vo.VideoPostBriefVO;
import com.inkneko.nekowindow.video.vo.VideoPostDetailVO;

import java.util.List;

public interface VideoService {

    /**
     * 视频投稿
     *
     * @param dto    视频信息
     * @param userId 上传者
     * @return 返回稿件ID
     */
    CreateVideoPostVO createVideoPost(CreateVideoPostDTO dto, Long userId);

    /**
     * 查询视频投稿
     *
     * @param nkid         稿件ID
     * @param viewerUserId 访问者用户ID
     * @return 指定稿件信息
     */
    VideoPost getVideoPost(Long nkid, Long viewerUserId);

    /**
     * 查询视频投稿简略信息，包括上传者，标签
     *
     * @param nkid         稿件ID
     * @param viewerUserId 访问者用户ID
     * @return 稿件
     */
    VideoPostBriefVO getVideoPostBrief(Long nkid, Long viewerUserId);

    /**
     * 获取指定用户的上传视频
     *
     * @param uid          用户id
     * @param viewerUserId 访问者用户ID
     * @param page         页数
     * @param size         页面大小
     * @return 用户已上传的视频列表，以时间倒序
     */
    List<VideoPost> getUploadedVideoPosts(Long uid, Long viewerUserId, Long page, Long size);

    /**
     * 查询视频详细信息
     *
     * @param nkid         视频ID
     * @param viewerUserId 访问者用户ID
     * @return 视频详细信息
     */
    VideoPostDetailVO getVideoPostDetail(Long nkid, Long viewerUserId);

    /**
     * 获取指定视频资源信息
     *
     * @param videoId      视频资源ID
     * @return 对应的视频资源
     */
    VideoPostResource getVideoPostResource(Long videoId);

    /**
     * 获取指定稿件的视频资源列表
     *
     * @param nkid 稿件ID
     * @return 指定稿件的视频资源列表
     */
    List<VideoPostResource> getVidePostResourcesByVideoPostId(Long nkid);

    /**
     * 查询所有的分区
     *
     * @return 分区列表
     */
    List<PartitionInfo> getPartitions();

    /**
     * 查询分区推荐标签
     *
     * @param partitionId 分区ID
     * @return 推荐标签列表
     */
    List<PartitionRecommendTag> getPartitionRecommendTags(Integer partitionId);

    /**
     * 查询分区的推荐视频
     *
     * @param partitionId 分区ID
     * @param uid         用户ID
     * @return 推荐视频列表
     */
    List<VideoPostBriefVO> getPartitionRecommendVideos(Integer partitionId, Long uid);

    /**
     * 查询分区视频
     *
     * @param partitionId 分区ID
     * @param page        页数
     * @param size        页面大小
     * @return 视频列表
     */
    List<VideoPostBriefVO> getPartitionVideos(Integer partitionId, Long page, Long size);

    /**
     * 更新视频信息
     *
     * @param dto 更新数据，见{@link UpdatePostBriefDTO}
     * @param uid 发起者用户uid
     */
    void updatePostBrief(UpdatePostBriefDTO dto, Long uid);

    /**
     * 更新视频资源信息
     *
     * @param dto 更新内容
     */
    void updateVideoPostResourceConversionState(UpdateVideoResourceConversionStateDTO dto);

    /**
     * 获取稿件的标签
     *
     * @param nkid 视频ID
     * @return 标签列表
     */
    List<String> getVideoPostTags(Long nkid);
}
