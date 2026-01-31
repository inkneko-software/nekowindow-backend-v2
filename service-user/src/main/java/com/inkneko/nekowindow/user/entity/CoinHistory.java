package com.inkneko.nekowindow.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoinHistory {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long uid;
    private Integer num;
    private Long bizId;
    private String bizType;
    private Long bizKey;
    private Date createdAt;
}
