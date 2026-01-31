package com.inkneko.nekowindow.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class CoinOrder {
    @TableId(type = IdType.AUTO)
    private Long orderId;
}
