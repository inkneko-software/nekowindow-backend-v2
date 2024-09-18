package com.inkneko.nekowindow.video.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoPostVideosVO {
    private Long videoId;
    private String title;
    private Integer visit;
    private String dashMpdUrl;
}
