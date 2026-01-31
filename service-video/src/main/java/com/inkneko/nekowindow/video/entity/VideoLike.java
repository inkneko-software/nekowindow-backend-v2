package com.inkneko.nekowindow.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoLike {
    /**
     * CREATE TABLE video_like(
     *   id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'id',
     *   uid BIGINT NOT NULL COMMENT '用户id',
     *   nkid BIGINT NOT NULL COMMENT '视频id',
     *   created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
     *   UNIQUE(uid, nkid)
     * ) ENGINE=InnoDB DEFAULT CHARSET utf8mb4;
     */

    @TableId(type = IdType.AUTO)
    Long id;
    Long uid;
    Long nkid;
    Date createdAt;


}
