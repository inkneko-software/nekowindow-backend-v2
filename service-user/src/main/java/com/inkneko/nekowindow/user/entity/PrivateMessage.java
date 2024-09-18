package com.inkneko.nekowindow.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class PrivateMessage {
    @TableId(type = IdType.AUTO)
    private Long messageId;
    private Long targetUid;
    private Long senderUid;
    private Boolean targetRead;
    private String content;
    private Date createdAt;
    private Date updatedAt;
}
