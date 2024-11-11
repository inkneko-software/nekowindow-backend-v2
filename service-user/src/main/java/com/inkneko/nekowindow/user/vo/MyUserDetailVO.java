package com.inkneko.nekowindow.user.vo;

import com.inkneko.nekowindow.user.entity.UserDetail;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyUserDetailVO {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long uid;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String sign;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer exp;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String gender;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Date birth;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String avatarUrl;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String bannerUrl;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer fans;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer subscribes;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer coins;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer followers;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Boolean isGotDailyBonus;

    public MyUserDetailVO(UserDetail userDetail, Boolean isGotDailyBonus){
        this.uid = userDetail.getUid();
        this.username = userDetail.getUsername();
        this.sign = userDetail.getSign();
        this.exp = userDetail.getExp();
        this.gender = userDetail.getGender();
        this.birth = userDetail.getBirth();
        this.avatarUrl = userDetail.getAvatarUrl();
        this.bannerUrl = userDetail.getBannerUrl();
        this.fans = userDetail.getFans();
        this.subscribes = userDetail.getSubscribes();
        this.coins = userDetail.getCoins();
        this.followers = userDetail.getFans();
        this.isGotDailyBonus = isGotDailyBonus;

    }
}
