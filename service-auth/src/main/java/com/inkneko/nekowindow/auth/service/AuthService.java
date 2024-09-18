package com.inkneko.nekowindow.auth.service;

import com.inkneko.nekowindow.auth.entity.Auth;

public interface AuthService {

    Auth getSession(String sessionToken);

    void saveSession(Auth auth);

    Auth newSession(Long uid);
}
