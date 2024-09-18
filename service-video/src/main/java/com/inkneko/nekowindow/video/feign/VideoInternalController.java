package com.inkneko.nekowindow.video.feign;


import com.inkneko.nekowindow.api.video.client.VideoFeignClient;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class VideoInternalController implements VideoFeignClient {
    @Operation(summary = "更新视频资源信息")
    @Override
    public void updateVideoResource(Long videoId, Long dashMpdUrl) {

    }
}
