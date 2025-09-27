package com.inkneko.nekowindow.dto;

import lombok.Data;

@Data
public class SendDanmakuDTO {
    Long chatRoomId;
    Float time;
    String content;
    Integer color;
    Integer type;
    Integer fontSize;
}
