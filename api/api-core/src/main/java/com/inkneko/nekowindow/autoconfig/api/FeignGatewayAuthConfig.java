package com.inkneko.nekowindow.autoconfig.api;

import com.alibaba.cloud.commons.io.IOUtils;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.common.util.GatewayAuthUtils;
import feign.FeignException;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Response;
import feign.codec.ErrorDecoder;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.json.GsonJsonParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Configuration
@EnableAutoConfiguration
public class FeignGatewayAuthConfig {
    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Bean
    public RequestInterceptor requestInterceptor(){
        logger.info("Loaded Feign Gateway Auth Plugin");
        return requestTemplate -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null){
                HttpServletRequest request =  attributes.getRequest();
                String userId = request.getHeader(GatewayAuthUtils.HEADER_USER_ID);
                String sessionToken = request.getHeader(GatewayAuthUtils.HEADER_SESSION);
                if (userId != null){
                    requestTemplate.header(GatewayAuthUtils.HEADER_USER_ID, userId);
                    requestTemplate.header(GatewayAuthUtils.HEADER_SESSION, sessionToken);
                }
            }
        };
    }

    @Bean
    public ErrorDecoder errorDecoder(){
        return new ErrorDecoder() {
            @Override
            public Exception decode(String methodKey, Response response) {
                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    try {
                        com.inkneko.nekowindow.common.Response<?> responseBody = objectMapper.readValue(response.body().asInputStream(), com.inkneko.nekowindow.common.Response.class);
                        throw new ServiceException(responseBody.getCode(), responseBody.getMessage());
                    }catch (DatabindException e){
                        logger.error("failed to cast error message to ServiceException while reading upstream service's error response. calling method: {}", methodKey, e);
                        return new ServiceException(response.status(), "内部服务调用错误，请稍后尝试");
                    }
                } catch (IOException e) {
                    logger.error("encounter IOException while reading upstream service's error response. method: {}", methodKey, e);
                    return new ServiceException(response.status(), "内部服务调用错误，请稍后尝试");
                }
            }
        };
    }
}
