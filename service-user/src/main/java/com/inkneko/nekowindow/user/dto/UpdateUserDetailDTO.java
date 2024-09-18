package com.inkneko.nekowindow.user.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserDetailDTO {
    @NotNull
    private Long uid;
    private String username;
    private String sign;
    private String gender;
    private Date birth;
    private String avatarUrl;
    private String bannerUrl;
}
