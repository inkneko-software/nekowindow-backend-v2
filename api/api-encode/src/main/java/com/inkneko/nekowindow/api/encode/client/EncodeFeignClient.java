package com.inkneko.nekowindow.api.encode.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient("nekowindow-service-encode")
public interface EncodeFeignClient {

    @PostMapping("/internal/encode/parseSourceVideo")
    void parseSourceVideo(String url);
}
