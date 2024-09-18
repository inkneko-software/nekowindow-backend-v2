package com.inkneko.nekowindow.user.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Relation {
    private Long targetUid;
    private Long followerUid;
    private Date createdAt;
}
