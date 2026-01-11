package com.inkneko.nekowindow.video.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoPostResource {
    @TableId(type = IdType.AUTO)
    Long videoId;
    Long nkid;
    String title;
    Long visit;
    Integer state;
    String reviewFailedReason;
    LocalDateTime createdAt;
    Integer convertState;
    String convertErrMsg;
    Integer duration;
    String sourceVideoUrl;
    String dashMpdUrl;
    LocalDateTime conversionAt;
    String videoAdaptions;
    String audioAdaptions;
}
