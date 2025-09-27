package com.inkneko.nekowindow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatRoom {
    @TableId(type = IdType.AUTO)
    private Long chatId;
    private Long videoId;
    private LocalDateTime createdAt;
}
