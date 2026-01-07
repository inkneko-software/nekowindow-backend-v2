package com.inkneko.nekowindow.api.video.client;

import com.inkneko.nekowindow.api.video.dto.UpdateVideoResourceConversionStateDTO;
import com.inkneko.nekowindow.api.video.dto.VideoPostDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient("nekowindow-service-video")
public interface VideoFeignClient {

    @GetMapping("/internal/video/updateVideoResource")
    void updateVideoResource(@RequestParam Long videoId, @RequestParam Long dashMpdUrl);

    @PostMapping("/internal/video/updateVideoResourceConversionState")
    void updateVideoResourceConversionState(UpdateVideoResourceConversionStateDTO dto);

    @GetMapping("/internal/video/getVideoPostBatch")
    Map<Long, VideoPostDTO> getVideoPostBatch(@RequestParam List<Long> nkidList, @RequestParam Long viewerUserId);

    @GetMapping("/internal/video/getVideoPost")
    VideoPostDTO getVideoPost(@RequestParam Long nkid, @RequestParam Long viewerUserId);
}
