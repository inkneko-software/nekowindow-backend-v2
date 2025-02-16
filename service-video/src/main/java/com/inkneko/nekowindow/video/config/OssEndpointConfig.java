package com.inkneko.nekowindow.video.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class OssEndpointConfig {
    @Value("${s3.endpoint}")
    public String endpoint;

    @Value("${s3.bucket}")
    private String bucket;
}
