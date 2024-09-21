package com.inkneko.nekowindow.video.controller;

import com.inkneko.nekowindow.common.Response;
import com.inkneko.nekowindow.common.util.GatewayAuthUtils;
import com.inkneko.nekowindow.video.dto.UpdatePostBriefDTO;
import com.inkneko.nekowindow.video.entity.VideoPost;
import com.inkneko.nekowindow.video.service.VideoService;
import com.inkneko.nekowindow.video.vo.UserUploadedVideoStatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/video/management")
public class VideoManagementController {
    VideoService videoService;

    public VideoManagementController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping("/getUploadedVideos")
    @Operation(summary = "获取已上传视频列表")
    public Response<List<UserUploadedVideoStatisticsVO>> getUploadedVideos(@RequestParam(defaultValue = "1") Long page, @RequestParam(defaultValue = "20") Long size){
        Long uid = GatewayAuthUtils.auth();
        List<VideoPost> userVideoPosts = videoService.getUploadedVideoPosts(uid, page, size);
        List<UserUploadedVideoStatisticsVO> convertedVos = userVideoPosts.stream().map(UserUploadedVideoStatisticsVO::new).toList();
        return new Response<>("ok", convertedVos);
    }

    @PostMapping("/updateVideoPost")
    @Operation(summary = "更新已上传的视频信息")
    public Response<?> updateVideoPost(@RequestBody UpdatePostBriefDTO dto){
        Long uid = GatewayAuthUtils.auth();
        videoService.updatePostBrief(dto, uid);
        return new Response<>("ok");
    }
}
