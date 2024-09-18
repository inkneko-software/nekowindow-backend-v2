package com.inkneko.nekowindow.user.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class UserDetail {
     @TableId
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
     private Integer coins;
}
