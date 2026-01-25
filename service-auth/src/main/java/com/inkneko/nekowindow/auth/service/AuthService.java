package com.inkneko.nekowindow.auth.service;

import com.inkneko.nekowindow.auth.entity.Auth;

public interface AuthService {

    /**
     * 判断session是否存在
     * @param uid 用户id
     * @param sessionToken sessionToken
     * @return  若存在则返回Auth，否则返回null
     */
    Auth getSession(Long uid, String sessionToken);

    /**
     * 创建session
     * @param uid 用户id
     * @return session
     */
    Auth newSession(Long uid);

    /**
     * 删除session
     * @param uid 用户id
     * @param sessionToken sessionToken
     */
    void removeSession(Long uid, String sessionToken);

    /**
     * 删除用户当前所有登录session
     * @param uid 用户id
     */
    void removeAllUserSession(Long uid);
}
