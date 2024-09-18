package com.inkneko.nekowindow.video.controller;

import com.inkneko.nekowindow.common.Response;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/videoManagement")
public class VideoManagementController {
    @GetMapping("/getUploadedVideos")
    @Operation(summary = "获取已上传视频列表")
    public Response<?> getUploadedVideos(){
        return null;
    }
}
