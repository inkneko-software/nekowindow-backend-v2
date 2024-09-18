package com.inkneko.nekowindow.api.user.client;

import com.inkneko.nekowindow.api.user.vo.UserVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "nekowindow-service-user")
public interface UserFeignClient {

    @GetMapping("/internal/user/{userId}")
    UserVo get(@PathVariable Long userId);
}
