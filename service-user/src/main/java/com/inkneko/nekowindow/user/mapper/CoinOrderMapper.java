package com.inkneko.nekowindow.user.mapper;

import com.inkneko.nekowindow.user.entity.CoinOrder;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

@Mapper
public interface CoinOrderMapper{

    @Insert("INSERT INTO coin_order () VALUES ()")
    @Options(useGeneratedKeys = true, keyProperty = "orderId")
    int insert(CoinOrder coinOrder);
}
