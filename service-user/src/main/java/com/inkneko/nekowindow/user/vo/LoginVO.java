package com.inkneko.nekowindow.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginVO {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String sessionToken;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;
}
