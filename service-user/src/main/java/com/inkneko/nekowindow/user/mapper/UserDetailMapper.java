package com.inkneko.nekowindow.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inkneko.nekowindow.user.entity.UserDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserDetailMapper extends BaseMapper<UserDetail> {

    @Update("UPDATE user_detail SET fans = fans + #{num} WHERE uid = #{userId}")
    void updateUserFansNum(Long userId, Integer num);

    @Update("UPDATE user_detail SET subscribes = subscribes + #{num} WHERE uid = #{userId}")
    void updateUserSubscribesNum(Long userId, Integer num);


}
