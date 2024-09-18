package com.inkneko.nekowindow.auth.service.impl;

import com.inkneko.nekowindow.auth.service.AuthService;
import com.inkneko.nekowindow.common.Response;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.auth.entity.Auth;
import org.apache.commons.codec.digest.DigestUtils;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    RedissonClient redissonClient;
    RMapCache<String, Auth> uidSessionMap;
    SecureRandom secureRandom;
    public AuthServiceImpl(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
        uidSessionMap = redissonClient.getMapCache("auth-uid-session");
        this.secureRandom = new SecureRandom();
    }

    @Override
    public Auth getSession(String sessionToken) {
        return uidSessionMap.get(sessionToken);
    }

    @Override
    public void saveSession(Auth auth) {
        uidSessionMap.put(auth.getSessionToken(), auth);
    }

    @Override
    public Auth newSession(Long uid) {
        Auth auth = new Auth();
        auth.setUserId(uid);
        auth.setSessionToken(computeSessionToken(uid));
        return auth;
    }

    private String computeSessionToken(Long uid) {
        String sessionId = String.format("%d-%s-%d", uid, UUID.randomUUID(), secureRandom.nextInt(1000000000));
        return DigestUtils.sha1Hex(sessionId);
    }
}
