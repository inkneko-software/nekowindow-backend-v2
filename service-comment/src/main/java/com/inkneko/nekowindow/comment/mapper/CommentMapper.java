package com.inkneko.nekowindow.comment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inkneko.nekowindow.comment.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
