package com.inkneko.nekowindow.comment.vo;

import com.inkneko.nekowindow.comment.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecondaryCommentVO {
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
    private Long replyCid;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long replySecondaryCid;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Date createdAt;

    public SecondaryCommentVO(Comment comment, CommentUserVO commentUserVO) {
        this.commentId = comment.getCommentId();
        this.commentUserVO = commentUserVO;
        this.oid = comment.getOid();
        this.oidType = comment.getOidType();
        this.content = comment.getContent();
        this.likesCount = comment.getLikesCount();
        this.replyCid = comment.getReplyCid();
        this.replySecondaryCid = comment.getReplySecondaryCid();
        this.createdAt = comment.getCreatedAt();
    }
}
