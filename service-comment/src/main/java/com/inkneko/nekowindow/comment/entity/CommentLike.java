package com.inkneko.nekowindow.comment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentLike {

    private Long commentId;
    private Long uid;
    private Date createdAt;
}
