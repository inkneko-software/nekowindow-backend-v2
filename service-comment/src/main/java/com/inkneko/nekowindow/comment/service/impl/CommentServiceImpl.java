package com.inkneko.nekowindow.comment.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inkneko.nekowindow.api.user.client.UserFeignClient;
import com.inkneko.nekowindow.api.user.vo.UserVo;
import com.inkneko.nekowindow.comment.dto.CreateCommentDTO;
import com.inkneko.nekowindow.comment.entity.Comment;
import com.inkneko.nekowindow.comment.entity.CommentLike;
import com.inkneko.nekowindow.comment.mapper.CommentLikeMapper;
import com.inkneko.nekowindow.comment.mapper.CommentMapper;
import com.inkneko.nekowindow.comment.service.CommentService;
import com.inkneko.nekowindow.comment.vo.*;
import com.inkneko.nekowindow.common.ServiceException;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    CommentMapper commentMapper;
    CommentLikeMapper commentLikeMapper;
    RedissonClient redissonClient;
    UserFeignClient userFeignClient;
    public CommentServiceImpl(
            CommentMapper commentMapper,
            CommentLikeMapper commentLikeMapper,
            RedissonClient redissonClient,
            UserFeignClient userFeignClient
    ) {
        this.commentMapper = commentMapper;
        this.commentLikeMapper = commentLikeMapper;
        this.redissonClient = redissonClient;
        this.userFeignClient = userFeignClient;
    }

    @Override
    public CommentVO createComment(CreateCommentDTO dto, Long userId) {
        if (dto.getContent().length() > 2000){
            throw new ServiceException(400, "评论内容过长，最大支持2000字符");
        }

        if (dto.getType() == Comment.CommentType.VIDEO){
            //redis校验

        }else if (dto.getType() == Comment.CommentType.ACTIVITY){
            throw new ServiceException(500, "活动评论暂未开放");
        }else{
            throw new ServiceException(400, "未知的评论类型");
        }

        if (dto.getReplyCid() != null){
            Comment replyComment = commentMapper.selectById(dto.getReplyCid());
            if (replyComment == null || replyComment.getIsDeleted()){
                throw new ServiceException(400, "回复的评论不存在或已被删除");
            }

            if (dto.getReplySecondaryCid() != null){
                Comment replySecondaryComment = commentMapper.selectById(dto.getReplySecondaryCid());
                if (replySecondaryComment == null || replySecondaryComment.getIsDeleted()){
                    throw new ServiceException(400, "回复的评论不存在或已被删除");
                }
            }
        }
        Comment comment = new Comment();
        comment.setUid(userId);
        comment.setOid(dto.getOid());
        comment.setOidType(Comment.CommentType.VIDEO);
        comment.setContent(dto.getContent());
        comment.setReplyCid(dto.getReplyCid());
        comment.setReplySecondaryCid(dto.getReplySecondaryCid());
        comment.setLikesCount(0);
        comment.setIsDeleted(false);
        commentMapper.insert(comment);

        UserVo userVo = userFeignClient.get(userId);
        CommentUserVO commentUserVO = new CommentUserVO(
                userVo.getUid(),
                userVo.getUsername(),
                userVo.getAvatarUrl()
        );

        return new CommentVO(comment, commentUserVO, null);
    }

    @Override
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null){
            throw new ServiceException(404, "评论不存在");
        }

        if (!comment.getUid().equals(userId)){
            throw new ServiceException(403, "无权限删除该评论");
        }
        comment.setIsDeleted(true);
        commentMapper.update(
                null,
                Wrappers.<Comment>lambdaUpdate()
                        .set(Comment::getIsDeleted, true)
                        .eq(Comment::getCommentId, commentId)
        );
    }

    @Override
    @Transactional
    public void likeComment(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null){
            throw new ServiceException(400, "评论不存在");
        }

        try{
            CommentLike commentLike = new CommentLike();
            commentLike.setCommentId(commentId);
            commentLike.setUid(userId);
            commentLikeMapper.insert(commentLike);

            commentMapper.update(
                    null,
                    Wrappers.<Comment>lambdaUpdate()
                            .eq(Comment::getCommentId, commentId)
                            .setSql("likes_count = likes_count + 1")
            );
        }catch(DuplicateKeyException ignored){

        }
    }

    @Override
    public void unlikeComment(Long commentId, Long userId) {
        commentLikeMapper.delete(
                Wrappers.<CommentLike>lambdaQuery()
                        .eq(CommentLike::getCommentId, commentId)
                        .eq(CommentLike::getUid, userId)
        );
    }

    @Override
    public CommentPageVO getCommentsByObjectId(Long objectId, Comment.CommentType idType, long page, long size) {
        if (page < 1 ){
            page = 1;
        }

        if (size <= 0 || size > 50){
            size = 50;
        }

        Long totalComments = commentMapper.selectCount(
                Wrappers.<Comment>lambdaQuery()
                        .eq(Comment::getOid, objectId)
                        .eq(Comment::getOidType, idType)
                        .eq(Comment::getReplyCid, 0)
        );

        CommentPageVO result = new CommentPageVO(
                totalComments,
                page,
                size,
                new ArrayList<>()
        );

        if (totalComments != 0){
            List<Comment> comments = commentMapper.selectPage(
                    new Page<>(page, size),
                    Wrappers.<Comment>lambdaQuery()
                            .eq(Comment::getOid, objectId)
                            .eq(Comment::getOidType, idType)
                            .eq(Comment::getReplyCid, 0)
            ).getRecords();

            List<CommentVO> commentVOS = new ArrayList<>();

            // 查询每条根评论的前两条二级评论，组成CommentVO
            for (Comment comment : comments){
                Long totalSecondaryComments = commentMapper.selectCount(
                        Wrappers.<Comment>lambdaQuery()
                                .eq(Comment::getReplyCid, comment.getCommentId())
                );

                List<SecondaryCommentVO> secondaryCommentVOS = new ArrayList<>();
                if (totalSecondaryComments != 0){
                    List<Comment> secondaryComments = commentMapper.selectList(
                            Wrappers.<Comment>lambdaQuery()
                                    .eq(Comment::getReplyCid, comment.getCommentId())
                                    .last("LIMIT 2")
                    );
                    secondaryCommentVOS = secondaryComments.stream().map(
                            secondaryComment -> {
                                UserVo secondaryUserVo = userFeignClient.get(secondaryComment.getUid());
                                CommentUserVO secondaryCommentUserVO = new CommentUserVO(
                                        secondaryUserVo.getUid(),
                                        secondaryUserVo.getUsername(),
                                        secondaryUserVo.getAvatarUrl()
                                );
                                return new SecondaryCommentVO(secondaryComment, secondaryCommentUserVO);
                            }
                    ).toList();
                }

                // 加载第一页二级评论
                SecondaryCommentPageVO secondaryCommentPageVO = new SecondaryCommentPageVO(
                        totalSecondaryComments,
                        1L,
                        2L,
                        secondaryCommentVOS
                );

                UserVo userVo = userFeignClient.get(comment.getUid());
                CommentUserVO commentUserVO = new CommentUserVO(
                        userVo.getUid(),
                        userVo.getUsername(),
                        userVo.getAvatarUrl()
                );
                CommentVO commentVO = new CommentVO(comment,commentUserVO, secondaryCommentPageVO);
                commentVOS.add(commentVO);
            }
            result.setComments(commentVOS);
        }

        return result;
    }

    @Override
    public Long getCommentsCountByObjectId(Long objectId, Comment.CommentType idType) {
        return commentMapper.selectCount(
                Wrappers.<Comment>lambdaQuery()
                        .eq(Comment::getOid, objectId)
                        .eq(Comment::getOidType, idType)
                        .eq(Comment::getReplyCid, 0)
        );
    }



    @Override
    public SecondaryCommentPageVO getSecondaryCommentsByCommentId(Long commentId, long page, long size) {
        if (page < 1 ){
            page = 1;
        }

        if ( size <= 0 || size > 10){
            size = 10;
        }

        Long totalSecondaryComments = commentMapper.selectCount(
                Wrappers.<Comment>lambdaQuery()
                        .eq(Comment::getReplyCid, commentId)
        );

        SecondaryCommentPageVO result = new SecondaryCommentPageVO(
                totalSecondaryComments,
                page,
                size,
                new ArrayList<>()
        );

        if (totalSecondaryComments > 0){
            List<Comment> secondaryComments = commentMapper.selectPage(
                    new Page<>(page, size),
                    Wrappers.<Comment>lambdaQuery()
                            .eq(Comment::getReplyCid, commentId)
            ).getRecords();
            List<SecondaryCommentVO> secondaryCommentVOS = secondaryComments.stream().map(
                    secondaryComment -> new SecondaryCommentVO(secondaryComment, getCommentUserVO(secondaryComment.getUid()))
            ).toList();
            result.setSecondaryComments(secondaryCommentVOS);
        }

        return result;
    }

    @Override
    public Long getSecondaryCommentsCountByCommentId(Long commentId) {
        return commentMapper.selectCount(
                Wrappers.<Comment>lambdaQuery()
                        .eq(Comment::getReplyCid, commentId)
        );
    }

    public CommentUserVO getCommentUserVO(Long userId){
        UserVo userVo = userFeignClient.get(userId);
        return new CommentUserVO(
                userVo.getUid(),
                userVo.getUsername(),
                userVo.getAvatarUrl()
        );
    }
}
