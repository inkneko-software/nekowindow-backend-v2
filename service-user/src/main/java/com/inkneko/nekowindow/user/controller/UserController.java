package com.inkneko.nekowindow.user.controller;


import com.inkneko.nekowindow.api.oss.client.OssFeignClient;
import com.inkneko.nekowindow.api.oss.dto.GenUploadUrlDTO;
import com.inkneko.nekowindow.api.oss.vo.UploadRecordVO;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.user.dto.*;
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
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;
    private final OssFeignClient ossFeignClient;

    public UserController(UserService userService, OssFeignClient ossFeignClient) {
        this.userService = userService;
        this.ossFeignClient = ossFeignClient;
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

    @PostMapping("/loginByEmailPassword")
    @Operation(summary = "登录", description = "通过邮箱与密码进行登录")
    public Response<?> loginByEmailPassword(@RequestBody EmailPasswordLoginDTO dto, HttpServletResponse response) {
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

    @PostMapping("/updatePasswordByOldPassword")
    @Operation(summary = "更新密码", description = "通过旧密码进行密码更新")
    public Response<?> updatePasswordByOldPassword(@Validated @RequestBody UpdatePasswordByOldPasswordDTO dto, HttpServletResponse response) {
        Long uid = GatewayAuthUtils.auth();
        LoginVO loginVo = userService.updatePasswordByOldPassword(uid, dto.getOldPassword(), dto.getNewPassword());
        Cookie userIdCookie = new Cookie("userId", loginVo.getUserId().toString());
        Cookie sessionTokenCookie = new Cookie("sessionToken", loginVo.getSessionToken());
        userIdCookie.setPath("/");
        //90天
        userIdCookie.setMaxAge(60 * 60 * 24 * 90);
        sessionTokenCookie.setPath("/");
        sessionTokenCookie.setMaxAge(60 * 60 * 24 * 90);
        response.addCookie(userIdCookie);
        response.addCookie(sessionTokenCookie);
        return new Response<>("重置成功");
    }

    @PostMapping("/sendPasswordResetEmailCode")
    @Operation(summary = "发送密码重置邮件")
    public Response<?> sendPasswordResetEmailCode(@RequestBody SendResetPasswordEmailCodeDTO dto) {
        userService.sendPasswordResetEmailCode(dto.getEmail());
        return new Response<>("发送成功");
    }

    @PostMapping("/updatePasswordByEmailCode")
    @Operation(summary = "更新密码", description = "通过邮箱与邮箱验证码进行密码更新")
    public Response<?> updatePasswordByEmailCode(@Validated @RequestBody UpdatePasswordByEmailCodeDTO dto, HttpServletResponse response) {
        LoginVO loginVo = userService.updatePasswordByEmailCode(dto.getEmail(), dto.getCode(), dto.getNewPassword());
        Cookie userIdCookie = new Cookie("userId", loginVo.getUserId().toString());
        Cookie sessionTokenCookie = new Cookie("sessionToken", loginVo.getSessionToken());
        userIdCookie.setPath("/");
        //90天
        userIdCookie.setMaxAge(60 * 60 * 24 * 90);
        sessionTokenCookie.setPath("/");
        sessionTokenCookie.setMaxAge(60 * 60 * 24 * 90);
        response.addCookie(userIdCookie);
        response.addCookie(sessionTokenCookie);
        return new Response<>("更新成功");
    }

    @PostMapping("/updateUserDetail")
    @Operation(summary = "更新用户资料")
    public Response<?> updateUserDetail(@Validated @RequestBody UpdateUserDetailDTO dto) {
        Long uid = GatewayAuthUtils.auth();
        UserDetail userDetail = new UserDetail();
        //头像连接校验
        if (dto.getAvatarUrl() != null) {
            Response<UploadRecordVO> uploadRecordVO = ossFeignClient.isURLValid(dto.getAvatarUrl());
            if (uploadRecordVO.getCode() == 404) {
                throw new ServiceException(400, "指定头像资源URL不存在");
            }
            UploadRecordVO uploadRecord = uploadRecordVO.getData();
            if (uploadRecord.getBucket().compareTo("nekowindow") != 0 || !uploadRecord.getObjectKey().startsWith("upload/avatar/")) {
                throw new ServiceException(400, "不正确的头像资源URL");
            }
            if (!uploadRecord.getUid().equals(uid)) {
                throw new ServiceException(400, "指定头像资源非当前用户上传");
            }
            userDetail.setAvatarUrl(dto.getAvatarUrl());
        }

        userDetail.setUid(uid);
        userDetail.setSign(dto.getSign());
        userDetail.setUsername(dto.getUsername());
        userDetail.setGender(dto.getGender());
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

    @PostMapping("/generateAvatarUploadURL")
    @Operation(summary = "获取头像上传链接")
    public Response<String> generateAvatarUploadURL() {
        Long uid = GatewayAuthUtils.auth();
        String uploadKey = DigestUtils.sha1Hex(String.format("%s-%d", UUID.randomUUID(), uid));
        return new Response<>("ok", ossFeignClient.genUploadUrl(new GenUploadUrlDTO("nekowindow", "upload/avatar/" + uploadKey)).getData().getUploadUrl());

    }
}
