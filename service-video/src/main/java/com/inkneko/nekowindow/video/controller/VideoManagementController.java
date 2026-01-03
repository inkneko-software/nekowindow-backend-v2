package com.inkneko.nekowindow.video.controller;

import com.inkneko.nekowindow.common.Response;
import com.inkneko.nekowindow.common.util.GatewayAuthUtils;
import com.inkneko.nekowindow.video.dto.UpdatePostBriefDTO;
import com.inkneko.nekowindow.video.entity.VideoPost;
import com.inkneko.nekowindow.video.service.VideoService;
import com.inkneko.nekowindow.video.vo.UserUploadedVideoStatisticsVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

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
        List<VideoPost> userVideoPosts = videoService.getUploadedVideoPosts(uid, uid, page, size);
        List<UserUploadedVideoStatisticsVO> convertedVos = userVideoPosts.stream()
                .map(post -> {
                    List<String> tags = videoService.getVideoPostTags(post.getNkid());
                    return new UserUploadedVideoStatisticsVO(
                            post,
                            tags,
                            videoService.getVidePostResourcesByVideoPostId(post.getNkid())
                    );
                })
                .toList();
        return new Response<>("ok", convertedVos);
    }

    @PostMapping("/updateVideoPost")
    @Operation(summary = "更新已上传的视频信息", description = "若更新成功，返回已更新的当前视频信息")
    public Response<UserUploadedVideoStatisticsVO> updateVideoPost(@RequestBody UpdatePostBriefDTO dto){
        Long uid = GatewayAuthUtils.auth();
        videoService.updatePostBrief(dto, uid);
        List<String> tags = videoService.getVideoPostTags(dto.getNkid());
        UserUploadedVideoStatisticsVO vo = new UserUploadedVideoStatisticsVO(
                videoService.getVideoPost(dto.getNkid(), uid),
                tags,
                videoService.getVidePostResourcesByVideoPostId(dto.getNkid())
        );
        return new Response<>("ok", vo);
    }

    @PostMapping("/deleteVideoPost")
    @Operation(summary = "删除已上传的视频")
    public Response<?> deleteVideoPost(@RequestParam Long nkid) {
        Long uid = GatewayAuthUtils.auth();
        videoService.deleteVideoPost(nkid, uid);
        return new Response<>("删除成功", null);
    }
}
