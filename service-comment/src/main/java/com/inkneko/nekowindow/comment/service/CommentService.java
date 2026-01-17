package com.inkneko.nekowindow.comment.service;

import com.inkneko.nekowindow.comment.dto.CreateCommentDTO;
import com.inkneko.nekowindow.comment.entity.Comment;
import com.inkneko.nekowindow.comment.vo.CommentPageVO;
import com.inkneko.nekowindow.comment.vo.CommentVO;
import com.inkneko.nekowindow.comment.vo.SecondaryCommentPageVO;
import com.inkneko.nekowindow.comment.vo.SecondaryCommentVO;

import java.util.List;

public interface CommentService {

    /**
     * 创建评论
     * @param dto 创建评论DTO
     * @param userId 用户ID
     * @return 评论视图对象
     */
    CommentVO createComment(CreateCommentDTO dto, Long userId);

    /**
     * 删除评论
     * @param commentId 评论ID
     * @param userId 操作者用户ID
     */
    void deleteComment(Long commentId, Long userId);

    /**
     * 点赞评论
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void likeComment(Long commentId, Long userId);

    /**
     * 取消点赞评论
     * @param commentId 评论ID
     * @param userId 用户ID
     */
    void unlikeComment(Long commentId, Long userId);

    /**
     * 根据对象ID获取评论列表
     * @param objectId 对象ID
     * @param idType 对象类型
     * @param page 页码
     * @param size 每页数量
     * @return 评论视图对象列表
     */
    CommentPageVO getCommentsByObjectId(Long objectId, Comment.CommentType idType, long page, long size);

    /**
     * 根据对象ID获取评论数量
     * @param objectId 对象ID
     * @param idType 对象类型
     * @return 评论数量
     */
    Long getCommentsCountByObjectId(Long objectId, Comment.CommentType idType);

    /**
     * 根据评论ID获取二级评论列表
     * @param commentId 根评论ID
     * @param page 页码
     * @param size 每页数量
     * @return 二级评论视图对象列表
     */
    SecondaryCommentPageVO getSecondaryCommentsByCommentId(Long commentId, long page, long size);

    /**
     * 根据根评论ID获取二级评论数量
     * @param commentId 根评论ID
     * @return 二级评论数量
     */
    Long getSecondaryCommentsCountByCommentId(Long commentId);

}
