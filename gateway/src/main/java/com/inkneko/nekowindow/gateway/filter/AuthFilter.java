package com.inkneko.nekowindow.gateway.filter;

import com.inkneko.nekowindow.api.auth.vo.AuthVo;
import com.inkneko.nekowindow.common.util.GatewayAuthUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthFilter implements GlobalFilter {

    WebClient webClient;

    public AuthFilter(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://nekowindow-service-auth").build();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpRequest.Builder mutedRequest = request.mutate()
                .headers(headers -> {
                    headers.remove(GatewayAuthUtils.HEADER_USER_ID);
                    headers.remove(GatewayAuthUtils.HEADER_SESSION);
                });
        //获取cookie
        MultiValueMap<String, HttpCookie> cookieList = request.getCookies();
        HttpCookie userId = cookieList.getFirst("userId");
        HttpCookie sessionToken = cookieList.getFirst("sessionToken");
        //检查cookie格式
        if (userId == null || sessionToken == null) {
            return chain.filter(exchange.mutate().request(mutedRequest.build()).build());
        }
        Long userIdNumeric = null;
        try {
            userIdNumeric = Long.parseLong(userId.getValue());
        } catch (NumberFormatException e) {
            return chain.filter(exchange.mutate().request(mutedRequest.build()).build());
        }

        return webClient.method(HttpMethod.GET)
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/auth/session")
                        .queryParam("userId", userId.getValue())
                        .queryParam("sessionToken", sessionToken.getValue())
                        .build())
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .toEntity(AuthVo.class)
                .flatMap(authResponseEntity -> {
                    AuthVo auth = authResponseEntity.getBody();
                    if (auth != null) {
                        mutedRequest
                                .header(GatewayAuthUtils.HEADER_USER_ID, auth.getUserId().toString())
                                .header(GatewayAuthUtils.HEADER_SESSION, auth.getSessionToken());
                    }
                    return chain.filter(exchange.mutate().request(mutedRequest.build()).build());
                })
                .onErrorResume(throwable -> chain.filter(exchange.mutate().request(mutedRequest.build()).build()));
    }
}
