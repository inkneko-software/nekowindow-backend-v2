package com.inkneko.nekowindow.comment.controller;

import com.inkneko.nekowindow.comment.dto.CreateCommentDTO;
import com.inkneko.nekowindow.comment.entity.Comment;
import com.inkneko.nekowindow.comment.service.CommentService;
import com.inkneko.nekowindow.comment.vo.CommentPageVO;
import com.inkneko.nekowindow.comment.vo.CommentVO;
import com.inkneko.nekowindow.comment.vo.SecondaryCommentPageVO;
import com.inkneko.nekowindow.common.Response;
import com.inkneko.nekowindow.common.util.GatewayAuthUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/comment")
public class CommentController {

    CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/add")
    @Operation(summary = "添加评论")
    public Response<CommentVO> addComment(@RequestBody CreateCommentDTO dto) {
        Long userId = GatewayAuthUtils.auth();
        return new Response<>("添加成功", commentService.createComment(dto, userId));
    }

    @GetMapping("/getComments")
    @Operation(summary = "获取对象ID的评论列表")
    public Response<CommentPageVO> getComment(
            @RequestParam Long objectId,
            @RequestParam Comment.CommentType idType,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "50") long size) {
        return new Response<>("ok",commentService.getCommentsByObjectId(objectId, idType, page, size));
    }

    @GetMapping("/getReplyComments")
    @Operation(summary = "获取根评论的回复评论列表")
    public Response<SecondaryCommentPageVO> getReplyComments(
            @RequestParam Long rootCommentId,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size) {
        return new Response<>("ok", commentService.getSecondaryCommentsByCommentId(rootCommentId, page, size));
    }

    @PostMapping("/like")
    @Operation(summary = "点赞评论")
    public Response<?> likeComment(@RequestParam Long commentId) {
        Long userId = GatewayAuthUtils.auth();
        commentService.likeComment(commentId, userId);
        return new Response<>("点赞成功");
    }

    @PostMapping("/unlike")
    @Operation(summary = "取消点赞评论")
    public Response<?> unlikeComment(@RequestParam Long commentId) {
        Long userId = GatewayAuthUtils.auth();
        commentService.unlikeComment(commentId, userId);
        return new Response<>("取消点赞成功");
    }

    @PostMapping("/delete")
    @Operation(summary = "删除评论")
    public Response<?> deleteComment(@RequestParam Long commentId) {
        Long userId = GatewayAuthUtils.auth();
        commentService.deleteComment(commentId, userId);
        return new Response<>("删除成功");
    }
}
