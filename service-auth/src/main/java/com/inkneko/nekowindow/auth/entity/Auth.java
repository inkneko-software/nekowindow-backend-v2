package com.inkneko.nekowindow.auth.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class Auth implements Comparable<Auth> {
    private Long userId;
    private String sessionToken;
    private ZonedDateTime createDate;
    private ZonedDateTime expireDate;

    @Override
    public int compareTo(@NotNull Auth auth) {
        if (this.sessionToken == null || auth.getSessionToken() == null){
            return -1;
        }
        return this.sessionToken.compareTo(auth.getSessionToken());
    }
}
