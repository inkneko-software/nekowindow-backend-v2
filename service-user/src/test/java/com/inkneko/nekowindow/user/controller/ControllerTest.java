package com.inkneko.nekowindow.user.controller;

import com.inkneko.nekowindow.api.oss.client.OssFeignClient;
import com.inkneko.nekowindow.api.oss.vo.UploadRecordVO;
import com.inkneko.nekowindow.autoconfig.api.FeignGatewayAuthConfig;
import com.inkneko.nekowindow.autoconfig.controller.ServiceExceptionHandler;
import com.inkneko.nekowindow.common.Response;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootTest
public class ControllerTest {
    @Autowired
    OssFeignClient ossFeignClient;

    @Test
    void testController() {
        Response<UploadRecordVO> response = ossFeignClient.isURLValid("http://localhost:9000/nekowindow/upload/avatar/asdasdasda");
        System.out.println(response);
    }
}
