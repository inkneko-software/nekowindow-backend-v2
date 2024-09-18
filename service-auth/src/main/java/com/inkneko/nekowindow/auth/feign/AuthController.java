package com.inkneko.nekowindow.auth.feign;

import com.inkneko.nekowindow.api.auth.client.AuthClient;
import com.inkneko.nekowindow.api.auth.vo.AuthVo;
import com.inkneko.nekowindow.auth.entity.Auth;
import com.inkneko.nekowindow.auth.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController implements AuthClient {

    AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public AuthVo getSession(Long userId, String sessionToken) {
        Auth auth = authService.getSession(sessionToken);
        if (auth != null && auth.getUserId().equals(userId)) {
            return new AuthVo(auth.getUserId(), auth.getSessionToken(), auth.getCreateDate(), auth.getExpireDate());
        }
        return null;
    }

    @Override
    public AuthVo newSession(Long userId) {
        Auth auth = authService.newSession(userId);
        authService.saveSession(auth);
        return new AuthVo(auth.getUserId(), auth.getSessionToken(), auth.getCreateDate(), auth.getExpireDate());
    }

//    @PostMapping("/auth/login")
//    public Response<?> login(@RequestBody EmailLoginDto dto, HttpServletResponse response) {
//        Auth auth = authService.login(dto);
//        if (auth != null) {
//            Cookie userIdCookie = new Cookie("userId", auth.getUserId().toString());
//            Cookie sessionTokenCookie = new Cookie("sessionToken", auth.getSessionToken());
//            userIdCookie.setPath("/");
//            //90天
//            userIdCookie.setMaxAge(60 * 60 * 24 * 90);
//            sessionTokenCookie.setPath("/");
//            sessionTokenCookie.setMaxAge(60 * 60 * 24 * 90);
//            response.addCookie(userIdCookie);
//            response.addCookie(sessionTokenCookie);
//        }else{
//            response.setStatus(401);
//        }
//        return new Response<>("登录成功");
//    }
}
