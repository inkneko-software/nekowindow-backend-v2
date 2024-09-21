package com.inkneko.nekowindow.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadUserVO {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String sign;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer fans;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String avatarUrl;
}
