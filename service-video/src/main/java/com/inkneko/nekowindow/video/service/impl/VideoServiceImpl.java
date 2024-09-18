package com.inkneko.nekowindow.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inkneko.nekowindow.api.encode.client.EncodeFeignClient;
import com.inkneko.nekowindow.api.oss.client.OssFeignClient;
import com.inkneko.nekowindow.api.oss.vo.UploadRecordVO;
import com.inkneko.nekowindow.api.user.client.UserFeignClient;
import com.inkneko.nekowindow.api.user.vo.UserVo;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.video.config.OssEndpointConfig;
import com.inkneko.nekowindow.video.dto.CreateVideoPostDto;
import com.inkneko.nekowindow.video.entity.*;
import com.inkneko.nekowindow.video.mapper.*;
import com.inkneko.nekowindow.video.service.VideoService;
import com.inkneko.nekowindow.video.vo.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class VideoServiceImpl implements VideoService {

    VideoPostMapper videoPostMapper;
    VideoPostResourceMapper videoPostResourceMapper;
    PartitionInfoMapper partitionInfoMapper;
    PartitionRecommendTagMapper partitionRecommendTagMapper;
    PostTagMapper postTagMapper;
    OssEndpointConfig ossEndpointConfig;
    EncodeFeignClient encodeFeignClient;
    OssFeignClient ossFeignClient;
    UserFeignClient userFeignClient;

    public VideoServiceImpl(
            VideoPostMapper videoPostMapper,
            OssFeignClient ossFeignClient,
            UserFeignClient userFeignClient,
            PartitionInfoMapper partitionInfoMapper,
            PartitionRecommendTagMapper partitionRecommendTagMapper,
            PostTagMapper postTagMapper,
            VideoPostResourceMapper videoPostResourceMapper,
            OssEndpointConfig ossEndpointConfig,
            EncodeFeignClient encodeFeignClient
    ) {
        this.videoPostMapper = videoPostMapper;
        this.ossFeignClient = ossFeignClient;
        this.partitionInfoMapper = partitionInfoMapper;
        this.userFeignClient = userFeignClient;
        this.partitionRecommendTagMapper = partitionRecommendTagMapper;
        this.postTagMapper = postTagMapper;
        this.videoPostResourceMapper = videoPostResourceMapper;
        this.ossEndpointConfig = ossEndpointConfig;
        this.encodeFeignClient = encodeFeignClient;
    }

    /**
     * 视频投稿
     *
     * @param dto    视频信息
     * @param userId 上传者
     * @return
     */
    @Override
    public CreateVideoPostVO createVideoPost(CreateVideoPostDto dto, Long userId) {
        //视频文件与封面文件来源校验
        Pattern pattern = Pattern.compile(ossEndpointConfig.endpoint + "/(.+?)/upload/(video|cover)/(.+?)");
        Matcher videoUrlMatcher = pattern.matcher(dto.getVideoUrl());
        Matcher coverUrlMatcher = pattern.matcher(dto.getCoverUrl());
        if (!videoUrlMatcher.matches() || !coverUrlMatcher.matches()) {
            throw new ServiceException(400, "请使用本服务提供的上传途径");
        }
        //确保使用的是nekowindow桶
        if (!videoUrlMatcher.group(1).equals("nekowindow") || !coverUrlMatcher.group(1).equals("nekowindow")) {
            throw new ServiceException(400, "请使用正确的视频链接");
        }
        //确保文件存在，且为文件上传者
        String coverKey = coverUrlMatcher.group(3);
        String videoKey = videoUrlMatcher.group(3);
        UploadRecordVO videoUploadRecordVO = ossFeignClient.isObjectExists("nekowindow", "upload/video/" + videoKey).getData();
        UploadRecordVO coverUploadRecordVO = ossFeignClient.isObjectExists("nekowindow", "upload/cover/" + coverKey).getData();

        if (videoUploadRecordVO == null || coverUploadRecordVO == null) {
            throw new ServiceException(400, "指定对象不存在");
        }

        if (!videoUploadRecordVO.getUid().equals(userId) || !coverUploadRecordVO.getUid().equals(userId)) {
            throw new ServiceException(403, "当前用户不是文件上传者");
        }

        Set<String> tagSet = new HashSet<>();

        //检查标签
        for (String tag : dto.getTags()) {
            tagSet.add(tag.strip());
        }

        //创建稿件
        VideoPost videoPost = new VideoPost();
        videoPost.setTitle(dto.getTitle());
        videoPost.setUid(userId);
        videoPost.setDescription(dto.getDescription());
        videoPost.setCoverUrl(dto.getCoverUrl());
        videoPost.setPartitionId(dto.getPartitionId());
        videoPost.setState(0);
        videoPostMapper.insert(videoPost);
        //保存视频标签
        for (String tag : tagSet) {
            postTagMapper.insert(new PostTag(videoPost.getNkid(), tag));
        }
        //保存稿件视频信息
        VideoPostVideos videoPostVideos = new VideoPostVideos(
                null,
                videoPost.getNkid(),
                dto.getTitle(),
                0,
                3,
                "",
                null,
                0,
                "",
                "",
                dto.getVideoUrl(),
                "",
                null
        );

        videoPostResourceMapper.insert(videoPostVideos);

        //发布转码任务
        //encodeFeignClient.parseSourceVideo(dto.getVideoUrl());
        return new CreateVideoPostVO(videoPost.getNkid());
    }

    /**
     * 查询视频投稿
     *
     * @param nkid
     * @return
     */
    @Override
    public VideoPostBriefVO getVideoPost(Long nkid) {
        VideoPost post = videoPostMapper.selectById(nkid);
        if (post == null) {
            throw new ServiceException(404, "查询稿件不存在");
        }
        List<PostTag> videoTags = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>().eq(PostTag::getNkid, nkid));
        List<String> tags = videoTags.stream().map(PostTag::getTagName).collect(Collectors.toList());
        UserVo userVo = userFeignClient.get(post.getUid());
        UploadUserVO uploadUserVo = new UploadUserVO(userVo.getUsername(), userVo.getUid(), userVo.getSign(), userVo.getFans(), userVo.getAvatarUrl());
        List<VideoPostVideosVO> videosVos = videoPostResourceMapper.selectList(new LambdaQueryWrapper<VideoPostVideos>().eq(VideoPostVideos::getNkid, post.getNkid()))
                .stream()
                .map(videoPostVideos -> new VideoPostVideosVO(videoPostVideos.getVideoId(), videoPostVideos.getTitle(), videoPostVideos.getVisit(), videoPostVideos.getSourceVideoUrl()))
                .collect(Collectors.toList());

        return new VideoPostBriefVO(
                post.getTitle(),
                post.getDescription(),
                uploadUserVo,
                tags,
                post.getCreatedAt()
        );
    }

    /**
     * 查询视频详细信息
     *
     * @param nkid 视频ID
     * @return 视频详细信息
     */
    @Override
    public VideoPostDetailVO getVideoPostDetail(Long nkid) {
        return null;
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
                .selectList(new LambdaQueryWrapper<VideoPost>().eq(VideoPost::getPartitionId, partitionId).last("LIMIT 10"))
                .stream()
                .map(videoPost -> {
                    UserVo userVo =  userFeignClient.get(videoPost.getUid());
                    UploadUserVO uploadUserVO = new UploadUserVO(userVo.getUsername(), userVo.getUid(), userVo.getSign(), userVo.getFans(), userVo.getAvatarUrl());
                    List<String> tags = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>().eq(PostTag::getNkid, videoPost.getNkid())).stream().map(PostTag::getTagName).toList();
                    return new VideoPostBriefVO(videoPost.getTitle(), videoPost.getDescription(), uploadUserVO, tags, videoPost.getCreatedAt());
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
        IPage<VideoPost> videoPostPage = videoPostMapper.selectPage(new Page<>(page, size), new LambdaQueryWrapper<VideoPost>().eq(VideoPost::getPartitionId, partitionId));
        List<VideoPost> videoPosts = videoPostPage.getRecords();
        List<VideoPostBriefVO> result = new ArrayList<>();
        for(VideoPost videoPost : videoPosts){
            UserVo userVo = userFeignClient.get(videoPost.getUid());
            UploadUserVO uploader = new UploadUserVO(userVo.getUsername(), userVo.getUid(), userVo.getSign(), userVo.getFans(), userVo.getAvatarUrl());
            List<String> tags = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>().eq(PostTag::getNkid, videoPost.getNkid())).stream().map(PostTag::getTagName).toList();
            result.add(new VideoPostBriefVO(
                    videoPost.getTitle(),
                    videoPost.getDescription(),
                    uploader,
                    tags,
                    videoPost.getCreatedAt()
            ));
        }
        return result;
    }
}
