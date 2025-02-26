package com.inkneko.nekowindow.video.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inkneko.nekowindow.video.entity.VideoPostResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface VideoPostResourceMapper extends BaseMapper<VideoPostResource> {

    @Select("SELECT * FROM video_post_resource where video_id = #{videoId} FOR UPDATE")
    VideoPostResource selectVideoPostResourceForUpdate(Long videoId);
}
