package com.inkneko.nekowindow.video.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
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
import com.inkneko.nekowindow.video.service.VideoService;
import com.inkneko.nekowindow.video.vo.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 检查用户提供的视频URL是否为站内资源，并且是上传者
     *
     * @param url    视频的文件URL
     * @param userId 用户id
     * @return 若文件为站内资源，且userId为该文件的上传者，返回false，否则返回true
     */
    private boolean isVideoUrlInvalid(String url, Long userId) {
        //检查链接是否为站内资源
        Pattern pattern = Pattern.compile(ossEndpointConfig.endpoint + "/nekowindow/upload/video/(.+?)");
        Matcher matcher = pattern.matcher(url);
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
        return !videoUploadRecordVO.getUid().equals(userId);
    }

    /**
     * 检查用户提供的封面URL是否为站内资源，并且是上传者
     *
     * @param url    封面图片的文件URL
     * @param userId 用户id
     * @return 若文件为站内资源，且userId为该文件的上传者，返回false，否则返回true
     */
    private boolean isCoverUrlInvalid(String url, Long userId) {
        //检查链接是否为站内资源
        Pattern pattern = Pattern.compile(ossEndpointConfig.endpoint + "/nekowindow/upload/cover/(.+?)");
        Matcher matcher = pattern.matcher(url);
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
        return !coverUploadRecordVO.getUid().equals(userId);
    }

    /**
     * 视频投稿
     *
     * @param dto    视频信息
     * @param userId 上传者
     * @return
     */
    @Override
    @Transactional
    public CreateVideoPostVO createVideoPost(CreateVideoPostDTO dto, Long userId) {
        if (isCoverUrlInvalid(dto.getCoverUrl(), userId)) {
            throw new ServiceException(400, "封面链接不正确");
        }

        if (isVideoUrlInvalid(dto.getVideoUrl(), userId)) {
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
        videoPost.setCoverUrl(dto.getCoverUrl());
        videoPost.setPartitionId(dto.getPartitionId());
        videoPost.setPartitionName(partitionInfo.getPartitionName());
        videoPost.setState(0);
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
                0,
                3,
                "",
                null,
                0,
                "",
                0,
                dto.getVideoUrl(),
                "",
                null,
                null,
                null
        );

        videoPostResourceMapper.insert(videoPostResource);

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
        List<VideoPostResourceVO> videosVos = videoPostResourceMapper.selectList(new LambdaQueryWrapper<VideoPostResource>().eq(VideoPostResource::getNkid, post.getNkid()))
                .stream()
                .map(videoPostResource -> new VideoPostResourceVO(videoPostResource.getVideoId(), videoPostResource.getTitle(), videoPostResource.getVisit(), videoPostResource.getSourceVideoUrl()))
                .toList();

        return new VideoPostBriefVO(
                post.getNkid(),
                post.getTitle(),
                post.getDescription(),
                post.getCoverUrl(),
                uploadUserVo,
                tags,
                post.getCreatedAt()
        );
    }

    /**
     * 获取指定用户的上传视频
     *
     * @param uid  用户id
     * @param page 页数
     * @param size 页面大小
     * @return 用户已上传的视频列表，以时间倒序
     */
    @Override
    public List<VideoPost> getUploadedVideoPosts(Long uid, Long page, Long size) {
        IPage<VideoPost> selectPage = videoPostMapper.selectPage(new Page<>(page, size), new LambdaQueryWrapper<VideoPost>().eq(VideoPost::getUid, uid).orderByDesc(VideoPost::getCreatedAt));
        return selectPage.getRecords();
    }

    /**
     * 查询视频详细信息
     *
     * @param nkid 视频ID
     * @return 视频详细信息
     */
    @Override
    public VideoPostDetailVO getVideoPostDetail(Long nkid) {
        //查询投稿
        VideoPost videoPost = videoPostMapper.selectById(nkid);
        if (videoPost == null) {
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


        return new VideoPostDetailVO(videoPost, uploadUserVO, postResourceVOs, tags);
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
                    UserVo userVo = userFeignClient.get(videoPost.getUid());
                    UploadUserVO uploadUserVO = new UploadUserVO(userVo.getUsername(), userVo.getUid(), userVo.getSign(), userVo.getFans(), userVo.getAvatarUrl());
                    List<String> tags = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>().eq(PostTag::getNkid, videoPost.getNkid())).stream().map(PostTag::getTagName).toList();
                    return new VideoPostBriefVO(videoPost.getNkid(), videoPost.getTitle(), videoPost.getDescription(), videoPost.getCoverUrl(), uploadUserVO, tags, videoPost.getCreatedAt());
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
        for (VideoPost videoPost : videoPosts) {
            UserVo userVo = userFeignClient.get(videoPost.getUid());
            UploadUserVO uploader = new UploadUserVO(userVo.getUsername(), userVo.getUid(), userVo.getSign(), userVo.getFans(), userVo.getAvatarUrl());
            List<String> tags = postTagMapper.selectList(new LambdaQueryWrapper<PostTag>().eq(PostTag::getNkid, videoPost.getNkid())).stream().map(PostTag::getTagName).toList();
            result.add(new VideoPostBriefVO(
                    videoPost.getNkid(),
                    videoPost.getTitle(),
                    videoPost.getDescription(),
                    videoPost.getCoverUrl(),
                    uploader,
                    tags,
                    videoPost.getCreatedAt()
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
        if (dto.getCoverUrl() != null) {
            if (isCoverUrlInvalid(dto.getCoverUrl(), uid)) {
                throw new ServiceException(400, "封面链接不正确");
            }
            videoPost.setCoverUrl(dto.getCoverUrl());
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
        }
    }
}
