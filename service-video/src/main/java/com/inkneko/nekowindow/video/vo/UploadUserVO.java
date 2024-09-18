package com.inkneko.nekowindow.video.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadUserVO {
    private String username;
    private Long userId;
    private String sign;
    private Integer fans;
    private String avatarUrl;
}
