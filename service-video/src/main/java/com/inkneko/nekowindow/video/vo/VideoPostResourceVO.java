package com.inkneko.nekowindow.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoPostResourceVO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long videoId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer visit;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String dashMpdUrl;
}
