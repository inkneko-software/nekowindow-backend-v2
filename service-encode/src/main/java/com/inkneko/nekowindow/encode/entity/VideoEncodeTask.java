package com.inkneko.nekowindow.encode.entity;

import com.inkneko.nekowindow.encode.dto.VideoSegmentEncodeDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoEncodeTask {

    //见数据库表设计
    public static int QUALITY_1080P_60 = 10;
    public static int QUALITY_1080P_HQ = 11;
    public static int QUALITY_1080P = 12;
    public static int QUALITY_720P = 20;
    public static int QUALITY_360P = 30;

    // 视频id
    private Long videoId;

    //视频质量代号
    private Integer videoQualityCode;

    // 原视频地址
    private String sourceVideoUrl;

    //转码完成的视频地址
    private String resultVideoUrl;

    // 分片编号，起始为0
    private Integer segmentIndex;

    // 总分片数
    private Integer segmentTotal;

    // 分片时长，以秒为单位
    private String segmentSize;

    // 目标编码格式，如h264
    private String targetCodec;

    // 目标码率，以比特为单位
    private String targetBitrate;

    // 目标高度，如720
    private Integer targetHeight;

    // 目标帧率
    private String targetFrameRate;

    // 转码失败原因
    private String encodeFailedReason;

    // 任务创建时间
    private LocalDateTime createdAt;

    // 开始转码时间，NULL为未开始
    private LocalDateTime startedAt;

    // 完成转码时间，NULL为未完成
    private LocalDateTime completeAt;

    public VideoEncodeTask(VideoSegmentEncodeDTO dto) {
        this.videoId = dto.getVideoId();
        this.videoQualityCode = dto.getVideoQualityCode();
        this.sourceVideoUrl = dto.getSourceVideoURL();
        this.segmentIndex = dto.getCurrentSegment();
        this.segmentTotal = dto.getTotalSegment();
        this.segmentSize = dto.getSegmentDuration().toString();
        this.targetCodec = dto.getTargetCodec();
        this.targetBitrate = dto.getTargetBitrate();
        this.targetFrameRate = dto.getTargetFrameRate();

        this.targetHeight = dto.getHeight();

        this.createdAt = null;
        this.startedAt = null;
        this.completeAt = null;
    }
}
