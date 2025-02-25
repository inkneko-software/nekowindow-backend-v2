package com.inkneko.nekowindow.api.encode.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("nekowindow-service-encode")
public interface EncodeFeignClient {

    @PostMapping("/internal/encode/parseSourceVideo")
    void parseSourceVideo(@RequestParam Long videoId, @RequestParam String sourceVideoUrl);
}
