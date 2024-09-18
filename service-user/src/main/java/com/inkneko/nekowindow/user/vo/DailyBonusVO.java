package com.inkneko.nekowindow.user.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyBonusVO {
    @Schema(description = "用户id")
    Long userId;
    @Schema(description = "是否成功获得了登录奖励。若当天已获得奖励，该值为false")
    Boolean isGotBonus;
    @Schema(description = "当前硬币数")
    Integer currentCoins;
}