package com.inkneko.nekowindow.comment.dto;

import com.inkneko.nekowindow.comment.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCommentDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "对象ID，视频ID或动态ID")
    private Long oid;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "评论类型，0 视频评论，1 动态评论")
    private Comment.CommentType type;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "评论内容，最大65535字符")
    private String content;
    @Schema(description = "被回复的根评论ID")
    private Long replyCid;
    @Schema(description = "被回复的二级评论ID")
    private Long replySecondaryCid;
}
