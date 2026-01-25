package com.inkneko.nekowindow.api.auth.client;

import com.inkneko.nekowindow.api.auth.vo.AuthVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("nekowindow-service-auth")
public interface AuthClient {

    /**
     * 查询指定会话是否存在
     * @param userId 用户id
     * @param sessionToken 会话token
     * @return 会话信息，若不存在则返回null
     */
    @GetMapping("/internal/auth/session")
    AuthVo getSession(@RequestParam Long userId, @RequestParam String sessionToken);

    /**
     * 创建会话
     * @param userId 用户id
     * @return 会话信息
     */
    @PostMapping("/internal/auth/session")
    AuthVo newSession(@RequestParam Long userId);

    /**
     * 删除用户会话
     * @param userId 用户id
     */
    @PostMapping("/internal/auth/session/remove")
    void removeUserSession(@RequestParam Long userId, @RequestParam String sessionToken);

    /**
     * 删除用户所有会话
     * @param userId 用户id
     */
    @PostMapping("/internal/auth/session/removeAll")
    void removeAllUserSession(@RequestParam Long userId);
}
