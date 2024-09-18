package com.inkneko.nekowindow.user.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyBonusRecord {
    private Long uid;
    private Integer coins;
    private Date createdAt;
}
