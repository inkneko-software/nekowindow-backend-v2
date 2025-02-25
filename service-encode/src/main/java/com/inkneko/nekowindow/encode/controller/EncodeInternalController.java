package com.inkneko.nekowindow.encode.controller;


import com.inkneko.nekowindow.api.encode.client.EncodeFeignClient;
import com.inkneko.nekowindow.encode.config.EncodeConfig;
import com.inkneko.nekowindow.encode.dto.ProbeRequestDTO;
import com.inkneko.nekowindow.encode.producer.ProbeProducer;
import com.inkneko.nekowindow.encode.service.EncodeService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EncodeInternalController implements EncodeFeignClient {
    EncodeService encodeService;
    ProbeProducer probeProducer;

    public EncodeInternalController(EncodeService encodeService, ProbeProducer probeProducer) {
        this.encodeService = encodeService;
        this.probeProducer = probeProducer;
    }

    @Operation(summary = "开始对新视频执行工作流，包括分析，编码，合并生成DASH描述文件")
    @Override
    public void parseSourceVideo(Long videoId, String sourceVideoUrl) {
        probeProducer.produceProbeMessage(videoId, sourceVideoUrl);
    }
}
