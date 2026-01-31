package com.inkneko.nekowindow.user.service;

import com.inkneko.nekowindow.user.dto.EmailLoginDTO;
import com.inkneko.nekowindow.user.dto.EmailPasswordLoginDTO;
import com.inkneko.nekowindow.user.dto.SendLoginEmailCodeDTO;
import com.inkneko.nekowindow.user.entity.UserDetail;
import com.inkneko.nekowindow.user.vo.DailyBonusVO;
import com.inkneko.nekowindow.user.vo.LoginVO;
import com.inkneko.nekowindow.user.vo.MyUserDetailVO;
import com.inkneko.nekowindow.user.vo.UserDetailVO;

import java.util.List;

public interface UserService {
    /**
     * 请求发送登录验证码。若未注册则发送注册验证码
     *
     * @param dto 请求参数
     */
    void sendLoginEmailCode(SendLoginEmailCodeDTO dto);

    /**
     * 通过邮箱与验证码进行登录，若未注册则进行注册，并同时登录
     *
     * @param dto 请求参数
     * @return 若邮箱与验证码匹配，返回session信息
     */
    LoginVO login(EmailLoginDTO dto);

    /**
     * 通过邮箱与密码进行登录，若未注册则返回错误
     *
     * @param dto 登录参数
     * @return 若邮箱与密码匹配，返回session信息
     */
    LoginVO login(EmailPasswordLoginDTO dto);

    /**
     * 修改用户密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 修改成功的session信息
     */
    LoginVO updatePasswordByOldPassword(Long userId, String oldPassword, String newPassword);

    /**
     * 发送密码重置验证码
     *
     * @param email 邮箱
     */
    void sendPasswordResetEmailCode(String email);

    /**
     * 通过邮箱与验证码进行密码重置
     *
     * @param email 邮箱
     * @param emailCode 邮箱验证码
     * @param newPassword 新密码
     * @return 修改成功的session信息
     */
    LoginVO updatePasswordByEmailCode(String email, String emailCode, String newPassword);

    /**
     * 查询用户信息
     *
     * @param userId 用户ID
     * @return 用户详细信息
     */
    UserDetail getUserDetail(Long userId);

    /**
     * 查询指定userId用户的详细资料，同时并尝试获取该用户的登录奖励
     *
     * @param userId 用户id
     * @return 用户详细资料以及登录奖励获得信息
     */
    MyUserDetailVO getMyUserDetail(Long userId);

    /**
     * 更新用户信息
     *
     * @param userDetail 用户信息
     */
    void updateUserDetail(UserDetail userDetail);

    /**
     * 关注用户
     * @param fromUserId 发起者的用户ID
     * @param toUserId 被关注者的用户ID
     */
    void subscribeUser(Long fromUserId, Long toUserId);

    /**
     * 取消关注用户
     * @param fromUserId 发起者的用户ID
     * @param toUserId 被关注者的用户ID
     */
    void unsubscribeUser(Long fromUserId, Long toUserId);

    /**
     * 查询用户的关注列表
     * @param userId 用户ID
     * @param page 页数，从1开始
     * @param size 页面大小
     * @return 指定用户的关注列表
     */
    List<UserDetailVO> getUserSubscribeList(Long userId, Integer page, Integer size);

    /**
     * 查询指定用户的粉丝列表
     * @param userId 用户ID
     * @param page 页数，从1开始
     * @param size 页面大小
     * @return 指定用户的粉丝列表
     */
    List<UserDetailVO> getUserFollowerList(Long userId, Integer page, Integer size);

    /**
     * 投币
     * @param userId 用户ID
     * @param nkid nkid
     * @param num 投币数量
     */
    void postVideoCoin(Long userId, Long nkid, Integer num);
}
