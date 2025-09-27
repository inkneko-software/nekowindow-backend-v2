package com.inkneko.nekowindow.video.feign;


import com.inkneko.nekowindow.api.video.client.VideoFeignClient;
import com.inkneko.nekowindow.api.video.dto.UpdateVideoResourceConversionStateDTO;
import com.inkneko.nekowindow.video.entity.VideoPostResource;
import com.inkneko.nekowindow.video.service.VideoService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VideoInternalController implements VideoFeignClient {

    @Autowired
    VideoService videoService;

    @Operation(summary = "更新视频资源信息")
    @Override
    public void updateVideoResource(Long videoId, Long dashMpdUrl) {

    }

    @Override
    @PostMapping("/internal/video/updateVideoResourceConversionState")
    public void updateVideoResourceConversionState(@RequestBody UpdateVideoResourceConversionStateDTO dto) {
        videoService.updateVideoPostResourceConversionState(dto);
    }
}
