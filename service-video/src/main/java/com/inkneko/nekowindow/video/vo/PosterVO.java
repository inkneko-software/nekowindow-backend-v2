package com.inkneko.nekowindow.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PosterVO {
    @Schema(description = "海报描述")
    private String description;

    @Schema(description = "海报图片")
    private String imageURL;

    @Schema(description = "活动跳转链接")
    private String activityURL;
}
