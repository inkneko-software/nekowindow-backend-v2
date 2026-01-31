package com.inkneko.nekowindow.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostVideoCoinDTO {
    @Schema(description = "视频id", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long nkid;

    @Schema(description = "硬币数量", requiredMode = Schema.RequiredMode.REQUIRED)
    @Max(value=2, message = "最多可投2枚硬币")
    @Min(value=1, message = "最少可投1枚硬币")
    private Integer coinNum;
}
