package com.inkneko.nekowindow.encode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AudioEncodeDTO {
    //视频ID
    private Long videoId;
    //音频质量代码
    private Integer audioQualityCode;
    //原视频资源链接
    private String sourceVideoURL;
    //目标编码格式
    private String targetCodec;
    //目标码率
    private String targetBitrate;
}
