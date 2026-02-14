package com.inkneko.nekowindow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityUserVO {

    private Long uid;
    private String username;
    private String avatarUrl;
}
