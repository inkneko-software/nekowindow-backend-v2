package com.inkneko.nekowindow.comment.vo;


import com.inkneko.nekowindow.comment.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 根评论视图对象
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentVO {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long commentId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private CommentUserVO commentUserVO;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long oid;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Comment.CommentType oidType;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer likesCount;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private SecondaryCommentPageVO initialSecondaryCommentPageVO;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Date createdAt;

    public CommentVO(Comment comment, CommentUserVO commentUserVO, SecondaryCommentPageVO initialSecondaryCommentPageVO) {
        this.commentId = comment.getCommentId();
        this.commentUserVO = commentUserVO;
        this.oid = comment.getOid();
        this.oidType = comment.getOidType();
        this.content = comment.getContent();
        this.likesCount = comment.getLikesCount();
        this.createdAt = comment.getCreatedAt();
        this.initialSecondaryCommentPageVO = initialSecondaryCommentPageVO;
    }
}
