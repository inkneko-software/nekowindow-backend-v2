package com.inkneko.nekowindow.encode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对视频的某个分片进行转码的请求参数
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoSegmentEncodeDTO {
    //视频ID
    private Long videoId;
    //视频质量代号
    Integer videoQualityCode;
    //原视频资源链接
    private String sourceVideoURL;
    //当前分段编号
    private Integer currentSegment;
    //总分段数
    private Integer totalSegment;
    //分段时长
    private Float segmentDuration;
    //总时长
    private Float totalDuration;
    //缩放参数
    private String scale;
    //目标码率
    private String targetBitrate;
    //目标编码格式
    private String targetCodec;
    //目标帧率
    private String targetFrameRate;
    //目标关键帧间隔
    private Integer targetGopSize;
    //目标高度
    private Integer height;



}
