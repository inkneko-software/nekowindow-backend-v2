package com.inkneko.nekowindow.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inkneko.nekowindow.api.encode.client.EncodeFeignClient;
import com.inkneko.nekowindow.api.oss.client.OssFeignClient;
import com.inkneko.nekowindow.api.oss.vo.UploadRecordVO;
import com.inkneko.nekowindow.api.user.client.UserFeignClient;
import com.inkneko.nekowindow.api.user.vo.UserVo;
import com.inkneko.nekowindow.api.video.dto.UpdateVideoResourceConversionStateDTO;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.video.config.OssEndpointConfig;
import com.inkneko.nekowindow.video.dto.CreateVideoPostDTO;
import com.inkneko.nekowindow.video.dto.UpdatePostBriefDTO;
import com.inkneko.nekowindow.video.entity.*;
import com.inkneko.nekowindow.video.mapper.*;
import com.inkneko.nekowindow.video.mq.producer.VideoPostCreatedProducer;
import com.inkneko.nekowindow.video.permission.policy.VideoPostResourceVisibilityPolicy;
import com.inkneko.nekowindow.video.permission.policy.VideoPostVisibilityPolicy;
import com.inkneko.nekowindow.video.permission.query.VideoPostQueryHelper;
import com.inkneko.nekowindow.video.permission.scene.VideoPostAccessScene;
import com.inkneko.nekowindow.video.permission.scene.VideoPostResourceAccessScene;
import com.inkneko.nekowindow.video.permission.state.VideoPostResourceState;
import com.inkneko.nekowindow.video.permission.state.VideoPostState;
import com.inkneko.nekowindow.video.service.VideoService;
import com.inkneko.nekowindow.video.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionListener;
import org.apache.rocketmq.spring.core.RocketMQLocalTransactionState;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.redisson.api.*;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {

    private final VideoPostCreatedProducer videoPostCreatedProducer;
    VideoPostMapper videoPostMapper;
    VideoPostResourceMapper videoPostResourceMapper;
    PartitionInfoMapper partitionInfoMapper;
    PartitionRecommendTagMapper partitionRecommendTagMapper;
    VideoCoinRecordMapper videoCoinRecordMapper;
    PostTagMapper postTagMapper;
    OssEndpointConfig ossEndpointConfig;
    EncodeFeignClient encodeFeignClient;
    OssFeignClient ossFeignClient;
    UserFeignClient userFeignClient;
    URI configURI;
    RedissonClient redissonClient;

    public VideoServiceImpl(
            VideoPostMapper videoPostMapper,
            OssFeignClient ossFeignClient,
            UserFeignClient userFeignClient,
            PartitionInfoMapper partitionInfoMapper,
            PartitionRecommendTagMapper partitionRecommendTagMapper,
            VideoCoinRecordMapper videoCoinRecordMapper,
            PostTagMapper postTagMapper,
            VideoPostResourceMapper videoPostResourceMapper,
            OssEndpointConfig ossEndpointConfig,
            EncodeFeignClient encodeFeignClient,
            RedissonClient redissonClient,
            VideoPostCreatedProducer videoPostCreatedProducer) {
        this.videoPostMapper = videoPostMapper;
        this.ossFeignClient = ossFeignClient;
        this.partitionInfoMapper = partitionInfoMapper;
        this.userFeignClient = userFeignClient;
        this.partitionRecommendTagMapper = partitionRecommendTagMapper;
        this.videoCoinRecordMapper = videoCoinRecordMapper;
        this.postTagMapper = postTagMapper;
        this.videoPostResourceMapper = videoPostResourceMapper;
        this.ossEndpointConfig = ossEndpointConfig;
        this.encodeFeignClient = encodeFeignClient;
        this.redissonClient = redissonClient;

        try {
            configURI = new URI(ossEndpointConfig.endpoint);
        } catch (Exception e) {
            throw new BeanInitializationException("OSS Endpoint配置的URI格式错误", e);
        }
        this.videoPostCreatedProducer = videoPostCreatedProducer;
    }

    /**
     * 检查用户提供的视频URL是否为站内资源，并且是上传者
     *
     * @param userURI 视频的文件URL
     * @param userId  用户id
     * @return 若文件为站内资源，且userId为该文件的上传者，返回true，否则返回false
     */
    private boolean isVideoUrlValid(URI userURI, Long userId) {
        //检查链接是否为站内资源
        if (!userURI.getHost().equals(configURI.getHost())) {
            return false;
        }
        Pattern pattern = Pattern.compile("^/nekowindow/upload/video/(.+)$");
        Matcher matcher = pattern.matcher(userURI.getPath());
        if (!matcher.matches()) {
            return false;
        }
        //检查是否存在
        String videoObjectKey = matcher.group(1);
        UploadRecordVO videoUploadRecordVO = ossFeignClient.isObjectExists("nekowindow", "upload/video/" + videoObjectKey).getData();
        if (videoUploadRecordVO == null) {
            return false;
        }
        //检查是否为上传者
        return videoUploadRecordVO.getUid().equals(userId);
    }

    /**
     * 检查用户提供的封面URL是否为站内资源，并且是上传者
     *
     * @param userURI 封面图片的文件URL
     * @param userId  用户id
     * @return 若文件为站内资源，且userId为该文件的上传者，返回true，否则返回false
     */
    private boolean isCoverUrlValid(URI userURI, Long userId) {
        //检查链接是否为站内资源
        if (!userURI.getHost().equals(configURI.getHost())) {
            return false;
        }
        Pattern pattern = Pattern.compile("^/nekowindow/upload/cover/(.+)$");
        Matcher matcher = pattern.matcher(userURI.getPath());
        if (!matcher.matches()) {
            return false;
        }
        //检查是否存在
        String coverObjectKey = matcher.group(1);
        UploadRecordVO coverUploadRecordVO = ossFeignClient.isObjectExists("nekowindow", "upload/cover/" + coverObjectKey).getData();
        if (coverUploadRecordVO == null) {
            return false;
        }
        //检查是否为上传者
        return coverUploadRecordVO.getUid().equals(userId);
    }

    /**
     * 视频投稿
     *
     * @param dto    视频信息
     * @param userId 上传者
     * @return 稿件信息
     */
    @Override
    @Transactional
    public CreateVideoPostVO createVideoPost(CreateVideoPostDTO dto, Long userId) {
        URI coverURI;
        URI videoURI;
        try {
            coverURI = new URI(dto.getCoverUrl());
            videoURI = new URI(dto.getVideoUrl());
        } catch (Exception e) {
            throw new ServiceException(400, "封面或视频链接格式不正确");
        }

        if (!isCoverUrlValid(coverURI, userId)) {
            throw new ServiceException(400, "封面链接不正确");
        }

        if (!isVideoUrlValid(videoURI, userId)) {
            throw new ServiceException(400, "视频链接不正确");
        }

        Set<String> tagSet = new HashSet<>();

        //检查标签
        for (String tag : dto.getTags()) {
            tagSet.add(tag.strip());
        }

        //检查分区
        PartitionInfo partitionInfo = partitionInfoMapper.selectById(dto.getPartitionId());
        if (partitionInfo == null) {
            throw new ServiceException(400, "指定分区不存在");
        }

        //创建稿件
        VideoPost videoPost = new VideoPost();
        videoPost.setTitle(dto.getTitle());
        videoPost.setUid(userId);
        videoPost.setDescription(dto.getDescription());
        videoPost.setCoverUrl(ossEndpointConfig.getEndpoint() + coverURI.getPath());
        videoPost.setPartitionId(dto.getPartitionId());
        videoPost.setPartitionName(partitionInfo.getPartitionName());
        videoPost.setState(VideoPostState.REVIEWING.getCode());
        videoPostMapper.insert(videoPost);
        //保存视频标签
        for (String tag : tagSet) {
            postTagMapper.insert(new PostTag(videoPost.getNkid(), tag));
        }
        //保存稿件视频信息
        VideoPostResource videoPostResource = new VideoPostResource(
                null,
                videoPost.getNkid(),
                dto.getTitle(),
                0L,
                3,
                "",
                null,
                0,
                "",
                0,
                ossEndpointConfig.getEndpoint() + videoURI.getPath(),
                "",
                null,
                null,
                null
        );

        videoPostResourceMapper.insert(videoPostResource);

        //发布转码任务
        encodeFeignClient.parseSourceVideo(videoPostResource.getVideoId(), ossEndpointConfig.getEndpoint() + videoURI.getPath());
        return new CreateVideoPostVO(videoPost.getNkid());
    }

    /**
     * 查询视频投稿
     *
     * @param nkid         稿件ID
     * @param viewerUserId 访问者用户ID
     * @return 稿件简略信息
     */
    @Override
    public VideoPostBriefVO getVideoPostBrief(Long nkid, Long viewerUserId) {
        VideoPost post = videoPostMapper.selectById(nkid);
        if (post == null || post.getState() != 0) {
            throw new ServiceException(404, "查询稿件不存在");
        }

        VideoPostAccessScene scene = VideoPostAccessScene.PUBLIC;
        if (viewerUserId != null && viewerUserId.equals(post.getUid())) {
            scene = VideoPostAccessScene.OWNER;
        }
        if (!VideoPostVisibilityPolicy.visibleStates(scene).contains(post.getState())) {
            throw new ServiceException(404, "查询稿件不存在");
        }

        List<PostTag> videoTags = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>().eq(PostTag::getNkid, nkid));
        List<String> tags = videoTags.stream().map(PostTag::getTagName).collect(Collectors.toList());
        UserVo userVo = userFeignClient.get(post.getUid());
        UploadUserVO uploadUserVo = new UploadUserVO(userVo.getUsername(), userVo.getUid(), userVo.getSign(), userVo.getFans(), userVo.getAvatarUrl());

        return new VideoPostBriefVO(
                post,
                uploadUserVo,
                tags
        );
    }


    @Override
    public List<VideoPost> getUploadedVideoPosts(Long uid, Long viewerUserId, Long page, Long size) {
        VideoPostAccessScene scene = VideoPostAccessScene.PUBLIC;
        if (viewerUserId != null && viewerUserId.equals(uid)) {
            scene = VideoPostAccessScene.OWNER;
        }

        IPage<VideoPost> selectPage = videoPostMapper.selectPage(
                new Page<>(page, size),
                VideoPostQueryHelper.visibleWrapper(scene, viewerUserId).eq(VideoPost::getUid, uid).orderByDesc(VideoPost::getCreatedAt)
        );
        return selectPage.getRecords();
    }

    @Override
    public VideoPostDetailVO getVideoPostDetail(Long nkid, Long viewerUserId) {
        //查询投稿
        VideoPost videoPost = videoPostMapper.selectById(nkid);
        if (videoPost == null) {
            throw new ServiceException(404, "稿件不存在");
        }

        VideoPostAccessScene scene = VideoPostAccessScene.PUBLIC;
        if (viewerUserId != null && viewerUserId.equals(videoPost.getUid())) {
            scene = VideoPostAccessScene.OWNER;
        }
        if (!VideoPostVisibilityPolicy.visibleStates(scene).contains(videoPost.getState())) {
            throw new ServiceException(404, "稿件不存在");
        }
        //查询上传者
        UserVo uploader = userFeignClient.get(videoPost.getUid());
        UploadUserVO uploadUserVO = new UploadUserVO(uploader);
        //查询视频资源
        List<VideoPostResource> postResources = videoPostResourceMapper.selectList(
                new LambdaQueryWrapper<VideoPostResource>().eq(VideoPostResource::getNkid, nkid)
        );
        List<VideoPostResourceVO> postResourceVOs = postResources.stream().map(VideoPostResourceVO::new).toList();
        //查询标签
        List<String> tags = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>().eq(PostTag::getNkid, nkid))
                .stream().map(PostTag::getTagName).toList();

        //TODO: 查询是否点赞

        // 查询是否已投币
        List<VideoCoinRecord> videoCoinRecords = videoCoinRecordMapper.selectList(
                Wrappers.<VideoCoinRecord>lambdaQuery()
                        .eq(VideoCoinRecord::getUid, viewerUserId)
                        .eq(VideoCoinRecord::getNkid, videoPost.getNkid())
        );
        int postedCoins = videoCoinRecords.stream().mapToInt(VideoCoinRecord::getNum).sum();

        return new VideoPostDetailVO(videoPost, uploadUserVO, postResourceVOs, tags, false, postedCoins);
    }

    @Override
    public VideoPostResource getVideoPostResource(Long videoId) {
        VideoPostResource videoPostResource = videoPostResourceMapper.selectById(videoId);
        if (videoPostResource == null) {
            throw new ServiceException(404, "指定视频资源不存在");
        }
        return videoPostResource;
    }

    @Override
    public List<VideoPostResource> getVidePostResourcesByVideoPostId(Long nkid) {
        return videoPostResourceMapper.selectList(new LambdaQueryWrapper<VideoPostResource>().eq(VideoPostResource::getNkid, nkid));
    }

    /**
     * 查询所有的分区
     *
     * @return 分区列表
     */
    @Override
    public List<PartitionInfo> getPartitions() {
        return partitionInfoMapper.selectList(new LambdaQueryWrapper<>());
    }

    /**
     * 查询分区推荐标签
     *
     * @param partitionId 分区id
     * @return 推荐标签列表
     */
    @Override
    public List<PartitionRecommendTag> getPartitionRecommendTags(Integer partitionId) {
        return partitionRecommendTagMapper.selectList(new LambdaQueryWrapper<PartitionRecommendTag>().eq(PartitionRecommendTag::getPartitionId, partitionId));
    }

    /**
     * 查询分区的推荐视频
     *
     * @param partitionId 分区ID
     * @return 推荐视频列表
     */
    @Override
    public List<VideoPostBriefVO> getPartitionRecommendVideos(Integer partitionId, Long uid) {

        return videoPostMapper
                .selectList(new LambdaQueryWrapper<VideoPost>().eq(VideoPost::getPartitionId, partitionId).eq(VideoPost::getState, 0).last("LIMIT 10"))
                .stream()
                .map(videoPost -> {
                    UserVo userVo = userFeignClient.get(videoPost.getUid());
                    UploadUserVO uploadUserVO = new UploadUserVO(userVo.getUsername(), userVo.getUid(), userVo.getSign(), userVo.getFans(), userVo.getAvatarUrl());
                    List<String> tags = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>().eq(PostTag::getNkid, videoPost.getNkid())).stream().map(PostTag::getTagName).toList();
                    return new VideoPostBriefVO(
                            videoPost,
                            uploadUserVO,
                            tags
                    );
                })
                .collect(Collectors.toList());
    }

    /**
     * 查询分区视频
     *
     * @param partitionId 分区ID
     * @param page        页数
     * @param size        页面大小
     * @return 视频列表
     */
    @Override
    public List<VideoPostBriefVO> getPartitionVideos(Integer partitionId, Long page, Long size) {
        IPage<VideoPost> videoPostPage = videoPostMapper.selectPage(new Page<>(page, size), new LambdaQueryWrapper<VideoPost>().eq(VideoPost::getPartitionId, partitionId).eq(VideoPost::getState, 0));
        List<VideoPost> videoPosts = videoPostPage.getRecords();
        List<VideoPostBriefVO> result = new ArrayList<>();
        for (VideoPost videoPost : videoPosts) {
            UserVo userVo = userFeignClient.get(videoPost.getUid());
            UploadUserVO uploader = new UploadUserVO(userVo.getUsername(), userVo.getUid(), userVo.getSign(), userVo.getFans(), userVo.getAvatarUrl());
            List<String> tags = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>().eq(PostTag::getNkid, videoPost.getNkid())).stream().map(PostTag::getTagName).toList();
            result.add(new VideoPostBriefVO(
                    videoPost,
                    uploader,
                    tags
            ));
        }
        return result;
    }

    /**
     * 更新视频信息
     *
     * @param dto 更新数据，见{@link UpdatePostBriefDTO}
     * @param uid 发起者用户uid
     */
    @Override
    @Transactional
    public void updatePostBrief(UpdatePostBriefDTO dto, Long uid) {
        VideoPost videoPost = videoPostMapper.selectById(dto.getNkid());
        if (videoPost == null) {
            throw new ServiceException(404, "稿件不存在");
        }

        if (!videoPost.getUid().equals(uid)) {
            throw new ServiceException(403, "当前登录用户没有权限修改指定稿件");
        }

        if (dto.getTitle() != null) {
            videoPost.setTitle(dto.getTitle());
        }
        if (dto.getDescription() != null) {
            videoPost.setDescription(dto.getDescription());
        }

        URI coverURI;
        try {
            coverURI = new URI(dto.getCoverUrl());
        } catch (Exception e) {
            throw new ServiceException(400, "封面链接格式不正确");
        }

        if (dto.getCoverUrl() != null) {
            if (!isCoverUrlValid(coverURI, uid)) {
                throw new ServiceException(400, "封面链接不正确");
            }
            videoPost.setCoverUrl(ossEndpointConfig.getEndpoint() + coverURI.getPath());
        }

        videoPostMapper.updateById(videoPost);

        if (dto.getTags() != null) {
            postTagMapper.delete(new LambdaQueryWrapper<PostTag>().eq(PostTag::getNkid, dto.getNkid()));
            for (String tag : dto.getTags()) {
                postTagMapper.insert(new PostTag(dto.getNkid(), tag));
            }
        }

//        if (dto.getVideoUrl() != null){
//            if (isVideoUrlInvalid(dto.getVideoUrl(), uid)){
//                throw new ServiceException(400, "视频链接不正确");
//            }
//        }
    }

    @Override
    @Transactional
    public void updateVideoPostResourceConversionState(UpdateVideoResourceConversionStateDTO dto) {
        VideoPostResource videoPostResource = videoPostResourceMapper.selectVideoPostResourceForUpdate(dto.getVideoId());
        if (videoPostResource != null) {
            videoPostResource.setConvertState(dto.getConvertState());
            videoPostResource.setConvertErrMsg(dto.getConvertErrMessage());
            videoPostResource.setDuration(dto.getDuration());
            videoPostResource.setDashMpdUrl(dto.getDashMpdUrl());
            videoPostResource.setConversionAt(dto.getConversionAt());
            videoPostResource.setVideoAdaptions(dto.getVideoAdaptions());
            videoPostResource.setAudioAdaptions(dto.getAudioAdaptions());

            // 更新数据库中的记录
            videoPostResourceMapper.updateById(videoPostResource);

            // 如果转换成功，则更新视频的时长
            if (dto.getConvertState() == 3) {

                // 更新视频的总时长
                List<VideoPostResource> resources = videoPostResourceMapper.selectList(new LambdaQueryWrapper<VideoPostResource>().eq(VideoPostResource::getNkid, videoPostResource.getNkid()));
                Integer totalDuration = resources.stream().mapToInt(VideoPostResource::getDuration).sum();
                VideoPost videoPost = videoPostMapper.selectById(videoPostResource.getNkid());
                if (videoPost != null) {
                    videoPost.setDuration(totalDuration);
                    // 如果是第一个视频且当前状态处于审核中，则暂时跳过审核，直接转为公开访问
                    if (resources.get(0).getVideoId().equals(dto.getVideoId())) {
                        if (videoPost.getState().equals(VideoPostState.REVIEWING.getCode())) {
                            videoPost.setState(VideoPostState.NORMAL.getCode());
                        }
                    }
                    videoPostMapper.updateById(videoPost);
                    // 通知视频投稿创建完成
                    videoPostCreatedProducer.send(videoPost);
                }
            }

        }

    }

    @Override
    public void deleteVideoPost(Long nkid, Long userId) {
        VideoPost videoPost = videoPostMapper.selectById(nkid);
        if (videoPost == null) {
            throw new ServiceException(404, "稿件不存在");
        }

        if (!videoPost.getUid().equals(userId)) {
            throw new ServiceException(403, "当前登录用户没有权限删除指定稿件");
        }
        videoPost.setState(VideoPostState.SELF_DELETE.getCode());
        videoPostMapper.updateById(videoPost);
        List<VideoPostResource> resources = videoPostResourceMapper.selectList(new LambdaQueryWrapper<VideoPostResource>().eq(VideoPostResource::getNkid, nkid));
        for (VideoPostResource resource : resources) {
            resource.setState(VideoPostResourceState.SELF_DELETE.getCode());
            videoPostResourceMapper.updateById(resource);
        }
    }

    @Override
    public VideoPost getVideoPost(Long nkid, Long viewerUserId) {
        VideoPost videoPost = videoPostMapper.selectById(nkid);
        if (videoPost == null) {
            throw new ServiceException(404, "稿件不存在");
        }

        VideoPostAccessScene scene = VideoPostAccessScene.PUBLIC;
        if (viewerUserId != null && viewerUserId.equals(videoPost.getUid())) {
            scene = VideoPostAccessScene.OWNER;
        }
        if (!VideoPostVisibilityPolicy.visibleStates(scene).contains(videoPost.getState())) {
            throw new ServiceException(404, "稿件不存在");
        }
        return videoPost;
    }

    @Override
    public List<String> getVideoPostTags(Long nkid) {
        VideoPost videoPost = videoPostMapper.selectById(nkid);
        if (videoPost == null) {
            throw new ServiceException(404, "稿件不存在");
        }
        return postTagMapper.selectList(new LambdaQueryWrapper<PostTag>().eq(PostTag::getNkid, nkid)).stream().map(PostTag::getTagName).toList();
    }

    @Override
    public void addVideoViewCount(Long videoResourceId, Long userId) {
        RSetCache<Long> viewSet = redissonClient.getSetCache("video:viewer:" + videoResourceId);
        if (viewSet.add(userId)) {
            RAtomicLong viewCount = redissonClient.getAtomicLong("video:view_count:" + videoResourceId);
            viewCount.incrementAndGet();
            redissonClient.getSet("video:view:dirty").add(videoResourceId);
        }
    }

}
