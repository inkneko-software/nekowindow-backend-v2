package com.inkneko.nekowindow.api.auth.client;

import com.inkneko.nekowindow.api.auth.vo.AuthVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("nekowindow-service-auth")
public interface AuthClient {
    @GetMapping("/internal/auth/session")
    AuthVo getSession(@RequestParam Long userId, @RequestParam String sessionToken);

    @PostMapping("/internal/auth/session")
    AuthVo newSession(@RequestParam Long userId);
}
