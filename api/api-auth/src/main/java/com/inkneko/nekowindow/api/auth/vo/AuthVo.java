package com.inkneko.nekowindow.api.auth.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthVo {
    private Long userId;
    private String sessionToken;
    private ZonedDateTime createDate;
    private ZonedDateTime expireDate;
}
