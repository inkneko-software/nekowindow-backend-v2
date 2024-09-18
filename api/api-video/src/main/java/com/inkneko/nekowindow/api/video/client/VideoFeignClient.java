package com.inkneko.nekowindow.api.video.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("nekowindow-service-video")
public interface VideoFeignClient {

    @GetMapping("/internal/video/updateVideoResource")
    void updateVideoResource(Long videoId, Long dashMpdUrl);
}
