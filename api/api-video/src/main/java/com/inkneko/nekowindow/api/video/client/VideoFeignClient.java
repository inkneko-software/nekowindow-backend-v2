package com.inkneko.nekowindow.api.video.client;

import com.inkneko.nekowindow.api.video.dto.UpdateVideoResourceConversionStateDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("nekowindow-service-video")
public interface VideoFeignClient {

    @GetMapping("/internal/video/updateVideoResource")
    void updateVideoResource(@RequestParam Long videoId, @RequestParam Long dashMpdUrl);

    @PostMapping("/internal/video/updateVideoResourceConversionState")
    void updateVideoResourceConversionState(UpdateVideoResourceConversionStateDTO dto);

}
