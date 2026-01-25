package com.inkneko.nekowindow.auth.service.impl;

import com.inkneko.nekowindow.auth.service.AuthService;
import com.inkneko.nekowindow.common.Response;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.auth.entity.Auth;
import org.apache.commons.codec.digest.DigestUtils;
import org.redisson.api.RMapCache;
import org.redisson.api.RSetCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    RedissonClient redissonClient;
    // 主键为uid + sessionToken，值为Auth
    RMapCache<String, Auth> sessionMap;

    SecureRandom secureRandom;
    public AuthServiceImpl(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        sessionMap = redissonClient.getMapCache("auth_service::session_map");
        this.secureRandom = new SecureRandom();
    }

    private String computeSessionToken(Long uid) {
        String sessionId = String.format("%d-%s-%d", uid, UUID.randomUUID(), secureRandom.nextInt(1000000000));
        return DigestUtils.sha1Hex(sessionId);
    }
    @Override
    public Auth getSession(Long uid, String sessionToken) {
        return sessionMap.get("%d-%s".formatted(uid, sessionToken));
    }

    @Override
    public Auth newSession(Long uid) {
        Auth auth = new Auth();
        auth.setUserId(uid);
        auth.setSessionToken(computeSessionToken(uid));
        auth.setCreateDate(ZonedDateTime.now());
        auth.setExpireDate(ZonedDateTime.now().plusDays(90));
        sessionMap.put("%d-%s".formatted(auth.getUserId(), auth.getSessionToken()), auth, 90, TimeUnit.DAYS);
        RSetCache<String> userSessionsSet = redissonClient.getSetCache("auth_service::user_sessions_set:%d".formatted(uid));
        userSessionsSet.add("%d-%s".formatted(auth.getUserId(), auth.getSessionToken()));
        return auth;
    }

    @Override
    public void removeSession(Long uid, String sessionToken) {
        sessionMap.remove("%d-%s".formatted(uid, sessionToken));
        RSetCache<String> userSessionsSet = redissonClient.getSetCache("auth_service::user_sessions_set:%d".formatted(uid));
        userSessionsSet.remove("%d-%s".formatted(uid, sessionToken));
    }

    @Override
    public void removeAllUserSession(Long uid) {
        RSetCache<String> userSessionsSet = redissonClient.getSetCache("auth_service::user_sessions_set:%d".formatted(uid));
        for (String sessionId : userSessionsSet) {
            sessionMap.remove(sessionId);
        }
        userSessionsSet.clear();
    }
}
