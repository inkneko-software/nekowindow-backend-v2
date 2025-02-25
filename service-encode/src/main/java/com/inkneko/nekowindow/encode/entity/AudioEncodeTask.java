package com.inkneko.nekowindow.encode.entity;

import com.inkneko.nekowindow.encode.dto.AudioEncodeDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AudioEncodeTask {

    //见数据库表设计
    public static int QUALITY_320K = 70;
    public static int QUALITY_128K = 71;


    // 视频id
    private Long videoId;

    //音频质量代码
    private Integer audioQualityCode;

    //原视频地址
    private String sourceVideoUrl;

    //转码音频地址
    private String resultAudioUrl;

    // 目标编码格式
    private String targetCodec;

    // 目标码率
    private String targetBitrate;

    // 转码失败原因
    private String encodeFailedReason;

    // 任务创建时间
    private LocalDateTime createdAt;

    // 开始转码时间，NULL为未开始
    private LocalDateTime startedAt;

    // 完成转码时间，NULL为未完成
    private LocalDateTime completeAt;

    public AudioEncodeTask(AudioEncodeDTO dto) {
        this.videoId = dto.getVideoId();
        this.audioQualityCode = dto.getAudioQualityCode();
        this.targetCodec = dto.getTargetCodec();
        this.targetBitrate = dto.getTargetBitrate();
        this.sourceVideoUrl = dto.getSourceVideoURL();
        this.encodeFailedReason = null; // 默认值，如果转码失败需要更新
        this.createdAt = LocalDateTime.now();
    }
}