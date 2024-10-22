package com.inkneko.nekowindow.video.controller;

import com.inkneko.nekowindow.api.oss.client.OssFeignClient;
import com.inkneko.nekowindow.api.oss.dto.GenUploadUrlDTO;
import com.inkneko.nekowindow.common.Response;
import com.inkneko.nekowindow.common.util.GatewayAuthUtils;
import com.inkneko.nekowindow.video.dto.CreateVideoPostDTO;
import com.inkneko.nekowindow.video.entity.PartitionInfo;
import com.inkneko.nekowindow.video.entity.PartitionRecommendTag;
import com.inkneko.nekowindow.video.entity.VideoPost;
import com.inkneko.nekowindow.video.service.VideoService;
import com.inkneko.nekowindow.video.vo.*;
import io.swagger.v3.oas.annotations.Operation;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/video")
public class VideoController {
    @Autowired
    private OssFeignClient ossFeignClient;


    @Autowired
    private VideoService videoService;

    @GetMapping("/generateUploadUrl")
    @Operation(summary = "生成视频上传链接", description = "生成用于上传视频文件的预签名链接。需要登录。使用返回的链接进行文件上传，请求方法为PUT，body为文件内容。")
    public Response<String> generateUploadUrl() {
        Long uid = GatewayAuthUtils.auth();
        String uploadKey = DigestUtils.sha1Hex(String.format("%s-%d", UUID.randomUUID(), uid));
        return new Response<>("ok", ossFeignClient.genUploadUrl(new GenUploadUrlDTO("nekowindow", "upload/video/" + uploadKey)).getData().getUploadUrl());
    }

    @GetMapping("/generateCoverUploadUrl")
    @Operation(summary = "生成封面上传链接", description = "生成用于封面上传的预签名链接。需要登录。使用返回的链接进行文件上传，请求方法为PUT，body为文件内容。")
    public Response<String> generateCoverUploadUrl() {

        Long uid = GatewayAuthUtils.auth();
        String uploadKey = DigestUtils.sha1Hex(String.format("%s-%d", UUID.randomUUID(), uid));
        return new Response<>("ok", ossFeignClient.genUploadUrl(new GenUploadUrlDTO("nekowindow", "upload/cover/" + uploadKey)).getData().getUploadUrl());
    }

    @PostMapping("/createVideoPost")
    @Operation(summary = "创建视频投稿")
    public Response<CreateVideoPostVO> createVideoPost(@RequestBody CreateVideoPostDTO dto) {
        Long uid = GatewayAuthUtils.auth();
        return new Response<>("创建成功", videoService.createVideoPost(dto, uid));
    }

    @GetMapping("/getVideoPostBrief")
    @Operation(summary = "查询视频信息")
    public Response<VideoPostBriefVO> getVideoPostBrief(@RequestParam Long nkid) {
        return new Response<>("ok", videoService.getVideoPost(nkid));
    }

    @GetMapping("/getVideoPostDetail")
    @Operation(summary = "获取视频详细信息")
    public Response<VideoPostDetailVO> getVideoPostDetail(@RequestParam Long nkid){
        return new Response<>("ok", videoService.getVideoPostDetail(nkid));
    }

    @GetMapping("/getPartitionList")
    @Operation(summary = "查询所有的分区")
    public Response<List<PartitionInfo>> getPartitionList() {
        return new Response<>("ok", videoService.getPartitions());
    }

    @GetMapping("/getPartitionRecommendTagList")
    @Operation(summary = "查询指定分区的推荐标签")
    public Response<List<String>> getPartitionRecommendTagList(@RequestParam Integer partitionId) {
        return new Response<>("ok", videoService.getPartitionRecommendTags(partitionId).stream().map(PartitionRecommendTag::getTagName).collect(Collectors.toList()));
    }

    @GetMapping("/getHomePageRecommend")
    @Operation(summary = "获取首页推荐")
    public Response<HomeRecommendVO> getHomeRecommend() {
        Long uid = GatewayAuthUtils.auth(false);
        List<PartitionInfo> partitions = videoService.getPartitions();
        List<List<VideoPostBriefVO>> partitionVideos = new ArrayList<>();
        for (PartitionInfo partitionInfo : partitions) {
            List<VideoPostBriefVO> videos = videoService.getPartitionRecommendVideos(partitionInfo.getPartitionId(), uid);
            partitionVideos.add(videos);
        }
        HomeRecommendVO homeRecommendVO = new HomeRecommendVO(
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>(),
                videoService.getPartitions(),
                partitionVideos
        );
        return new Response<>("ok", homeRecommendVO);
    }

    @GetMapping("/getPartitionVideos")
    @Operation(summary = "查询指定分区视频，以时间倒序")
    public Response<List<VideoPostBriefVO>> getPartitionLatestVideo(@RequestParam Integer partitionId,
                                                                    @RequestParam(defaultValue = "1") Long page,
                                                                    @RequestParam(defaultValue = "10") Long size) {
        return new Response<>("ok", videoService.getPartitionVideos(partitionId, page, size));
    }

    @GetMapping("/getUploadedVideos")
    @Operation(summary = "获取已上传视频列表")
    public Response<List<UserUploadedVideoStatisticsVO>> getUserPosts(@RequestParam Long uid, @RequestParam(defaultValue = "1") Long page, @RequestParam(defaultValue = "20") Long size){
        List<VideoPost> userVideoPosts = videoService.getUploadedVideoPosts(uid, page, size);
        List<UserUploadedVideoStatisticsVO> convertedVos = userVideoPosts.stream().map(UserUploadedVideoStatisticsVO::new).toList();
        return new Response<>("ok", convertedVos);
    }


}
