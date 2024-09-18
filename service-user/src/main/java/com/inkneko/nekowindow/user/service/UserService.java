package com.inkneko.nekowindow.user.service;

import com.inkneko.nekowindow.user.dto.EmailLoginDTO;
import com.inkneko.nekowindow.user.dto.SendLoginEmailCodeDTO;
import com.inkneko.nekowindow.user.entity.UserDetail;
import com.inkneko.nekowindow.user.vo.DailyBonusVO;
import com.inkneko.nekowindow.user.vo.LoginVO;
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
     * 查询用户信息
     *
     * @param userId 用户ID
     * @return 用户详细信息
     */
    UserDetail getUserDetail(Long userId);

    /**
     * 更新用户信息
     *
     * @param userDetail 用户信息
     */
    void updateUserDetail(UserDetail userDetail);

    /**
     * 获取每日登录奖励
     *
     * @param uid 用户id
     * @return 每日奖励信息
     */
    DailyBonusVO doDailyBonus(Long uid);

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

    

}
