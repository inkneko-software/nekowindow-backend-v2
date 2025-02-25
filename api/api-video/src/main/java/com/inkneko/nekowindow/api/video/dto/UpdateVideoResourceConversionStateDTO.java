package com.inkneko.nekowindow.api.video.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateVideoResourceConversionStateDTO {
    private Long videoId;
    private Integer convertState;
    private String convertErrMessage;
    /**
     * duration VARCHAR(255) COMMENT '视频时长',
     *     source_video_url VARCHAR(255) NOT NULL COMMENT '原视频',
     *     dash_mpd_url VARCHAR(255) NOT NULL COMMENT 'mpd文件',
     *     conversion_at TIMESTAMP COMMENT '开始转码时间',
     *     source_resolution VARCHAR(255) NOT NULL DEFAULT 'unknow' COMMENT '视频分辨率',
     *     available_resolution
     */
    private String duration; // 视频时长
    private String sourceVideoUrl; // 原视频
    private String dashMpdUrl; // mpd文件
    private String conversionAt; // 开始转码时间，建议使用 java.time.LocalDateTime
    private String sourceResolution = "unknow"; // 视频分辨率，默认值为 'unknow'
    private String availableResolution; // 可用分辨率
}
