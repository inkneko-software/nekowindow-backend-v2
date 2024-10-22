package com.inkneko.nekowindow.video.vo;

import com.inkneko.nekowindow.api.user.vo.UserVo;
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

    public UploadUserVO(UserVo userVo) {
        this.username = userVo.getUsername();
        this.userId = userVo.getUid();
        this.sign = userVo.getSign();
        this.fans = userVo.getFans();
        this.avatarUrl = userVo.getAvatarUrl();
    }

}
