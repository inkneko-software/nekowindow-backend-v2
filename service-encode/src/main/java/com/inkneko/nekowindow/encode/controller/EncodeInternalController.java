package com.inkneko.nekowindow.encode.controller;


import com.inkneko.nekowindow.api.encode.client.EncodeFeignClient;
import com.inkneko.nekowindow.encode.service.EncodeService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EncodeInternalController implements EncodeFeignClient {
    @Autowired
    EncodeService encodeService;

    @Operation(summary = "新视频")
    @Override
    public void parseSourceVideo(String url) {
        encodeService.parseSourceVideo(url);
    }
}
