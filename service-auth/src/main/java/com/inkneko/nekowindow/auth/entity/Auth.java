package com.inkneko.nekowindow.auth.entity;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class Auth {
    private Long userId;
    private String sessionToken;
    private ZonedDateTime createDate;
    private ZonedDateTime expireDate;
}
