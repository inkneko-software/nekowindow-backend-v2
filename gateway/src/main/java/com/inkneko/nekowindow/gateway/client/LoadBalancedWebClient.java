package com.inkneko.nekowindow.gateway.client;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class LoadBalancedWebClient {

    @Bean
    @LoadBalanced
    public WebClient.Builder loadbalancedWebClientBuilder(){
        return WebClient.builder();
    }
}
