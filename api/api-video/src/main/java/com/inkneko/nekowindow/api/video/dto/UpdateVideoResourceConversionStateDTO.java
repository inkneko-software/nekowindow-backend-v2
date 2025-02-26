package com.inkneko.nekowindow.api.video.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateVideoResourceConversionStateDTO {
    private Long videoId;
    private Integer convertState;
    private String convertErrMessage;
    private Integer duration; // 视频时长
    private String dashMpdUrl; // mpd文件
    private LocalDateTime conversionAt; // 开始转码时间，建议使用 java.time.LocalDateTime
    private String videoAdaptions;
    private String audioAdaptions;

}
