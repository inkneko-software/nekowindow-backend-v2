package com.inkneko.nekowindow.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;


@Configuration
public class CorsConfig {

    //见配置文件
//    @Bean
//    public CorsWebFilter corsWebFilter() {
//        CorsConfiguration corsConfig = new CorsConfiguration();
//        corsConfig.addAllowedOriginPattern("*"); // Allow all origins
//        corsConfig.addAllowedMethod("*"); // Allow all HTTP methods
//        corsConfig.addAllowedHeader("*"); // Allow all headers
//        corsConfig.setAllowCredentials(true);
//
//        return new CorsWebFilter(source -> new CorsConfiguration(corsConfig));
//    }
}
