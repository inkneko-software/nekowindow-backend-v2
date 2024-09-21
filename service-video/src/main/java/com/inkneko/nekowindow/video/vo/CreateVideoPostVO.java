package com.inkneko.nekowindow.video.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateVideoPostVO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long nkid;
}
