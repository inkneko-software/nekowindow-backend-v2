package com.inkneko.nekowindow.user;

import com.inkneko.nekowindow.autoconfig.api.FeignGatewayAuthConfig;
import com.inkneko.nekowindow.autoconfig.controller.ServiceExceptionHandler;
import com.inkneko.nekowindow.user.mq.template.VideoCoinExtRocketMQTemplate;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.apache.rocketmq.spring.autoconfigure.RocketMQAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.inkneko.nekowindow.api")
@OpenAPIDefinition(info = @Info(title = "service-user", description = "用户服务API接口说明", version = "0.0.1"))
@EnableAsync
@Import({FeignGatewayAuthConfig.class, ServiceExceptionHandler.class, VideoCoinExtRocketMQTemplate.class, RocketMQAutoConfiguration.class})
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}