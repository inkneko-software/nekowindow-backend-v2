package com.inkneko.nekowindow.encode;

import com.inkneko.nekowindow.autoconfig.api.FeignGatewayAuthConfig;
import com.inkneko.nekowindow.autoconfig.controller.ServiceExceptionHandler;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.inkneko.nekowindow.api")
@Import({FeignGatewayAuthConfig.class, ServiceExceptionHandler.class})
@OpenAPIDefinition(info = @Info(title = "service-encode", description = "转码服务API接口说明" , version = "0.0.1"))
public class EncodeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EncodeServiceApplication.class, args);
    }
}
