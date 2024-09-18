package com.inkneko.nekowindow.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class UserCredential {
    @TableId(type = IdType.AUTO)
    private Long uid;
    private String authSalt;
    private String authHash;
    private String email;
    private Date createdAt;
    private Date updatedAt;
}
