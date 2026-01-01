package com.inkneko.nekowindow.operation;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "service-operation", description = "运营服务API接口说明", version = "0.0.1"))
public class OperationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OperationServiceApplication.class, args);
    }
}
