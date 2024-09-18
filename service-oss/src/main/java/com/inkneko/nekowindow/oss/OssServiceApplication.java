package com.inkneko.nekowindow.oss;

import com.inkneko.nekowindow.autoconfig.api.FeignGatewayAuthConfig;
import com.inkneko.nekowindow.autoconfig.controller.ServiceExceptionHandler;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({FeignGatewayAuthConfig.class, ServiceExceptionHandler.class})
@OpenAPIDefinition(info = @Info(title = "service-oss", description = "对象存储服务API接口说明"))
public class OssServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OssServiceApplication.class, args);
    }

}
