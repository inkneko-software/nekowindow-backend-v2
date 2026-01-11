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
public class VideoPost {
    @TableId(type = IdType.AUTO)
    Long nkid;
    Long uid;
    String title;
    String coverUrl;
    Integer duration;
    Long visit;
    Integer shared;
    String description;
    Integer state;
    Integer partitionId;
    String partitionName;
    String reviewFailedReason;
    Date createdAt;
}
