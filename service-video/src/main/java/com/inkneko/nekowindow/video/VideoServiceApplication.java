package com.inkneko.nekowindow.video;

import com.inkneko.nekowindow.autoconfig.api.FeignGatewayAuthConfig;
import com.inkneko.nekowindow.autoconfig.controller.ServiceExceptionHandler;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.inkneko.nekowindow.api")
@Import({FeignGatewayAuthConfig.class, ServiceExceptionHandler.class, RocketMQAutoConfiguration.class})
@OpenAPIDefinition(info = @Info(title = "service-video", description = "视频服务API接口说明", version = "0.0.1"))
@EnableScheduling
public class VideoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideoServiceApplication.class, args);
    }
}