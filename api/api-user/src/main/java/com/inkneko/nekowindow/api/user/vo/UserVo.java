package com.inkneko.nekowindow.api.user.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVo {
    private Long uid;
    private String username;
    private String sign;
    private Integer exp;
    private String gender;
    private Date birth;
    private String avatarUrl;
    private String bannerUrl;
    private Integer fans;
    private Integer subscribes;
}
