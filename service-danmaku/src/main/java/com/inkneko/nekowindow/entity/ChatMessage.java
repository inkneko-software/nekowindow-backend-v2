package com.inkneko.nekowindow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    /**
     * message_id BIGINT PRIMARY KEY COMMENT '雪花算法ID',
     *     chat_id BIGINT NOT NULL COMMENT '所属聊天室',
     *     progress FLOAT UNSIGNED NOT NULL COMMENT '弹幕在视频中发送的时间',
     *     user_id BIGINT UNSIGNED NOT NULL COMMENT '发送者id',
     *     content VARCHAR(255) NOT NULL COMMENT '弹幕内容',
     *     message_type TINYINT NOT NULL COMMENT '弹幕类型 0为普通滚动弹幕，1为顶部悬停弹幕，2为底部悬停弹幕',
     *     color_hex INT UNSIGNED NOT NULL COMMENT '弹幕颜色 RGB888格式',
     *     created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
     */
    @TableId(type = IdType.ASSIGN_ID)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long messageId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long chatId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Float progress;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer messageType;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer colorHex;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime createdAt;
}
