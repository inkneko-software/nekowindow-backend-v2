package com.inkneko.nekowindow.comment.entity;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Comment {

    @Getter
    @AllArgsConstructor
    public enum CommentType {
        VIDEO(0),
        ACTIVITY(1);

        @EnumValue
        private final int code;
    }

    @TableId(type = IdType.AUTO)
    private Long commentId;
    private Long uid;
    private Long oid;
    private CommentType oidType;
    private String content;
    private Long replyCid;
    private Long replySecondaryCid;
    private Integer likesCount;
    private Boolean isDeleted;
    private Date createdAt;
}
