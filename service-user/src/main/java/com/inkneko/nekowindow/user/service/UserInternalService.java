package com.inkneko.nekowindow.user.service;

import com.inkneko.nekowindow.api.user.vo.UserVo;

import java.util.List;

public interface UserInternalService {
    /**
     * 获取用户全部订阅列表
     * @param userId 用户id
     * @return 订阅列表
     */
    List<UserVo> getUserSubscribeList(Long userId);

    /**
     * 获取用户全部粉丝列表
     * @param userId 用户id
     * @return 粉丝列表
     */
    List<UserVo> getUserFansList(Long userId);

}
