package com.inkneko.nekowindow.video.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssEndpointConfig {
    @Value("${s3.endpoint}")
    public String endpoint;
}
