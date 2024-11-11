package com.inkneko.nekowindow.user.controller;


import com.inkneko.nekowindow.api.auth.client.AuthClient;
import com.inkneko.nekowindow.user.dto.EmailLoginDTO;
import com.inkneko.nekowindow.user.dto.SendLoginEmailCodeDTO;
import com.inkneko.nekowindow.user.dto.UpdateUserDetailDTO;
import com.inkneko.nekowindow.user.vo.DailyBonusVO;
import com.inkneko.nekowindow.user.vo.LoginVO;
import com.inkneko.nekowindow.user.vo.MyUserDetailVO;
import com.inkneko.nekowindow.user.vo.UserDetailVO;
import com.inkneko.nekowindow.common.Response;
import com.inkneko.nekowindow.common.util.GatewayAuthUtils;
import com.inkneko.nekowindow.user.entity.UserDetail;
import com.inkneko.nekowindow.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    UserService userService;
    AuthClient authClient;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/sendLoginEmailCode")
    @Operation(summary = "发送登录邮件", description = "发送登录邮件，若当前邮箱未注册，则发送注册邮件")
    public Response<?> sendLoginEmailCode(@RequestBody SendLoginEmailCodeDTO dto) {
        userService.sendLoginEmailCode(dto);
        return new Response<>("发送成功");
    }

    @PostMapping("/login")
    @Operation(summary = "登录", description = "通过邮箱与验证码进行登录")
    public Response<?> login(@RequestBody EmailLoginDTO dto, HttpServletResponse response) {
        LoginVO loginVo = userService.login(dto);
        Cookie userIdCookie = new Cookie("userId", loginVo.getUserId().toString());
        Cookie sessionTokenCookie = new Cookie("sessionToken", loginVo.getSessionToken());
        userIdCookie.setPath("/");
        //90天
        userIdCookie.setMaxAge(60 * 60 * 24 * 90);
        sessionTokenCookie.setPath("/");
        sessionTokenCookie.setMaxAge(60 * 60 * 24 * 90);
        response.addCookie(userIdCookie);
        response.addCookie(sessionTokenCookie);
        return new Response<>("登录成功");
    }

    @PostMapping("/updateUserDetail")
    @Operation(summary = "更新用户资料")
    public Response<?> updateUserDetail(@Validated @RequestBody UpdateUserDetailDTO dto) {
        Long uid = GatewayAuthUtils.auth();

        UserDetail userDetail = new UserDetail();
        userDetail.setUid(uid);
        userDetail.setSign(dto.getSign());
        userDetail.setUsername(dto.getUsername());
        userDetail.setGender(dto.getGender());
        userDetail.setAvatarUrl(dto.getAvatarUrl());
        userDetail.setBannerUrl(dto.getBannerUrl());
        userDetail.setBirth(dto.getBirth());
        userService.updateUserDetail(userDetail);
        return new Response<>("更新成功");
    }

    @GetMapping("/getUserDetail")
    @Operation(summary = "查询用户资料")
    public Response<UserDetailVO> getUserDetail(@RequestParam Long uid) {
        UserDetail userDetail = userService.getUserDetail(uid);
        if (userDetail == null) {
            return new Response<>(404, "用户不存在");
        }

        UserDetailVO userDetailVo = new UserDetailVO(userDetail);
        return new Response<>("ok", userDetailVo);
    }

    @GetMapping("/myUserDetail")
    @Operation(summary = "查询当前用户的个人资料")
    public Response<MyUserDetailVO> myUserDetail(HttpServletRequest request) {
        Long uid = GatewayAuthUtils.auth(request);
        if (uid == null) {
            return new Response<>(403, "请先登录");
        }

        return new Response<>("ok", userService.getMyUserDetail(uid));
    }
}
