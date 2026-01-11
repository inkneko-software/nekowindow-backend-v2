package com.inkneko.nekowindow.video.feign;


import com.inkneko.nekowindow.api.video.client.VideoFeignClient;
import com.inkneko.nekowindow.api.video.dto.UpdateVideoResourceConversionStateDTO;
import com.inkneko.nekowindow.api.video.dto.VideoPostDTO;
import com.inkneko.nekowindow.video.entity.VideoPostResource;
import com.inkneko.nekowindow.video.service.VideoInternalService;
import com.inkneko.nekowindow.video.service.VideoService;
import com.inkneko.nekowindow.video.vo.VideoPostBriefVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal/video")
public class VideoInternalController implements VideoFeignClient {

    VideoService videoService;
    VideoInternalService videoInternalService;

    public VideoInternalController(VideoService videoService, VideoInternalService videoInternalService) {
        this.videoService = videoService;
        this.videoInternalService = videoInternalService;
    }

    @Operation(summary = "更新视频资源信息")
    @Override
    public void updateVideoResource(Long videoId, Long dashMpdUrl) {

    }

    @Override
    @PostMapping("/updateVideoResourceConversionState")
    public void updateVideoResourceConversionState(@RequestBody UpdateVideoResourceConversionStateDTO dto) {
        videoService.updateVideoPostResourceConversionState(dto);
    }

    @Override
    @GetMapping("/getVideoPostBatch")
    public Map<Long, VideoPostDTO> getVideoPostBatch(List<Long> nkidList, Long viewerUserId) {
        return videoInternalService.getVideoPostBatch(nkidList, viewerUserId);
    }

    @Override
    @GetMapping("/getVideoPost")
    public VideoPostDTO getVideoPost(Long nkid, Long viewerUserId) {
        return videoInternalService.getVideoPost(nkid, viewerUserId);
    }
}
