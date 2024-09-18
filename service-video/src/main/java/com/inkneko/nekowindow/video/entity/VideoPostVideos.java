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
public class VideoPostVideos {
    @TableId(type = IdType.AUTO)
    Long videoId;
    Long nkid;
    String title;
    Integer visit;
    Integer state;
    String reviewFailedReason;
    Date createdAt;
    Integer convertState;
    String convertErrMsg;
    String duration;
    String sourceVideoUrl;
    String dashMpdUrl;
    Date conversionAt;
}
