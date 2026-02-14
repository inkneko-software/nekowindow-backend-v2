package com.inkneko.nekowindow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkneko.nekowindow.api.oss.client.OssFeignClient;
import com.inkneko.nekowindow.api.oss.vo.UploadRecordVO;
import com.inkneko.nekowindow.api.user.client.UserFeignClient;
import com.inkneko.nekowindow.api.user.vo.UserVo;
import com.inkneko.nekowindow.api.video.client.VideoFeignClient;
import com.inkneko.nekowindow.api.video.dto.VideoPostDTO;
import com.inkneko.nekowindow.common.Response;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.dto.CreateActivityDTO;
import com.inkneko.nekowindow.dto.UpdateActivityDTO;
import com.inkneko.nekowindow.entity.Activity;
import com.inkneko.nekowindow.mapper.ActivityMapper;
import com.inkneko.nekowindow.service.ActivityService;
import com.inkneko.nekowindow.vo.ActivityUserVO;
import com.inkneko.nekowindow.vo.ActivityVO;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {

    ActivityMapper activityMapper;
    UserFeignClient userFeignClient;
    OssFeignClient ossFeignClient;
    ObjectMapper objectMapper;
    RedissonClient redissonClient;
    VideoFeignClient videoFeignClient;
    public ActivityServiceImpl(
            ActivityMapper activityMapper,
            UserFeignClient userFeignClient,
            OssFeignClient ossFeignClient,
            ObjectMapper objectMapper,
            RedissonClient redissonClient,
            VideoFeignClient videoFeignClient
    ) {
        this.activityMapper = activityMapper;
        this.userFeignClient = userFeignClient;
        this.ossFeignClient = ossFeignClient;
        this.objectMapper = objectMapper;
        this.redissonClient = redissonClient;
        this.videoFeignClient = videoFeignClient;
    }

    @Override
    public ActivityVO saveActivity(CreateActivityDTO dto, Long userId) {

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (String image : dto.getImages()) {
                try {
                    Response<UploadRecordVO> response = ossFeignClient.isURLValid(image);
                    if (response.getCode() != 0) {
                        throw new ServiceException(400, "图片地址无效");
                    }
                } catch (ServiceException e) {
                    throw e;
                }
            }
        }
        String images = "[]";
        try {
            images = objectMapper.writeValueAsString(dto.getImages());
        } catch (Exception e) {
            throw new ServiceException(400, "图片地址无效");
        }

        Activity activity = new Activity(
                null,
                userId,
                dto.getTitle(),
                dto.getContent(),
                0,
                0L,
                dto.getRefNkid(),
                images,
                dto.getIsPublic() ? 0 : 1,
                0,
                null

        );

        activityMapper.insert(activity);
        return toActivityVO(activity, userId);
    }

    @Override
    public ActivityVO saveVideoPostActivity(Long nkid, Long userId) {
        RLock lock = redissonClient.getLock("activity:saveVideoPostActivity:" + nkid);
        boolean locked = false;
        try {
            locked = lock.tryLock(5, 5, TimeUnit.SECONDS);
            if (!locked) {
                throw new ServiceException(500, "服务器繁忙，请稍后再试");
            }
            Activity activity = activityMapper.selectOne(
                    new LambdaQueryWrapper<Activity>()
                            .eq(Activity::getObjectId, nkid)
                            .eq(Activity::getActivityType, 1)
            );
            if (activity != null) {
                return toActivityVO(activity, userId);
            }
            activity = new Activity();
            activity.setActivityType(1);
            activity.setObjectId(nkid);
            activity.setUid(userId);
            activity.setContent("");
            activity.setImages("[]");
            activityMapper.insert(activity);
            return toActivityVO(activity, userId);
        } catch (InterruptedException e) {
            throw new ServiceException(500, "内部服务错误，请稍后再试");
        }
        finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    public ActivityVO getActivityById(Long id, Long viewerUserId) {
        Activity activity = activityMapper.selectById(id);
        if (
                activity == null ||
                        activity.getState() == 2 ||
                        (activity.getState() == 1 && !activity.getUid().equals(viewerUserId))
        ) {
            throw new ServiceException(404, "未找到该动态");
        }

        return toActivityVO(activity, viewerUserId);
    }

    @Override
    public void deleteActivity(Long id, Long userId) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            return;
        }
        if (activity.getActivityType() != 0) {
            throw new ServiceException(400, "无法删除非文章动态");
        }
        if (!activity.getUid().equals(userId)) {
            throw new ServiceException(403, "无权修改该动态");
        }
        activityMapper.update(
                null,
                Wrappers.<Activity>lambdaUpdate()
                        .eq(Activity::getId, id)
                        .set(Activity::getState, 2)
        );
    }

    @Override
    public void updateActivity(UpdateActivityDTO dto, Long uid) {
        Activity activity = activityMapper.selectById(dto.getId());
        if (activity == null || activity.getState() == 2) {
            throw new ServiceException(404, "未找到该动态");
        }

        if (!activity.getUid().equals(uid)) {
            throw new ServiceException(403, "无权修改该动态");
        }

        if (dto.getContent() != null && dto.getContent().isEmpty()) {
            throw new ServiceException(400, "动态内容不能为空");
        }

        if (dto.getImages() != null && !dto.getImages().isEmpty()) {
            for (String image : dto.getImages()) {
                try {
                    Response<UploadRecordVO> response = ossFeignClient.isURLValid(image);
                    if (response.getCode() != 0) {
                        throw new ServiceException(400, "图片地址无效");
                    }
                } catch (ServiceException e) {
                    throw new ServiceException(400, "图片地址无效");
                }
            }
        }

        String images = "[]";
        try {
            images = objectMapper.writeValueAsString(dto.getImages());
        } catch (Exception e) {
            throw new ServiceException(400, "图片地址无效");
        }

        activity = new Activity();
        activity.setId(dto.getId());
        activity.setTitle(dto.getTitle());
        activity.setContent(dto.getContent());
        activity.setImages(images);
        activity.setState(dto.getIsPublic() ? 0 : 1);
        activityMapper.updateById(activity);
    }

    @Override
    public List<ActivityVO> getUserActivities(Long userId, Long viewerUserId, Long lastActivityId, long size) {
        LambdaQueryWrapper<Activity> queryWrapper = Wrappers.<Activity>lambdaQuery()
                .eq(Activity::getUid, userId);

        if (userId.equals(viewerUserId)) {
            queryWrapper.ne(Activity::getState, 2);
        } else {
            queryWrapper.eq(Activity::getState, 0);
        }

        if (lastActivityId != null) {
            queryWrapper.lt(Activity::getId, lastActivityId);
        }

        queryWrapper.orderByDesc(Activity::getId).last("LIMIT " + size);

        List<Activity> activities = activityMapper.selectList(queryWrapper);
        return activities.stream().map(activity -> toActivityVO(activity, viewerUserId)).toList();
    }

    @Override
    public List<ActivityVO> getUserTimeLine(Long userId, Long lastActivityId, long size) {
        List<UserVo> subscribeList = userFeignClient.getSubscribeList(userId);
        if (subscribeList.isEmpty()) {
            return List.of();
        }

        List<Long> userIdList = subscribeList.stream().map(UserVo::getUid).toList();
        LambdaQueryWrapper<Activity> queryWrapper = Wrappers.<Activity>lambdaQuery()
                .in(Activity::getUid, userIdList)
                .eq(Activity::getState, 0)
                .lt(lastActivityId != null, Activity::getId, lastActivityId)
                .or(wrapper -> wrapper.eq(Activity::getUid, userId).ne(Activity::getState, 2).lt(lastActivityId != null, Activity::getId, lastActivityId));
        queryWrapper.orderByDesc(Activity::getId).last("LIMIT " + size);
        List<Activity> activities = activityMapper.selectList(queryWrapper);
        return activities.stream().map(activity -> toActivityVO(activity, userId)).toList();
    }

    private ActivityVO toActivityVO(Activity activity, Long viewerUserId) {

        UserVo user = userFeignClient.get(activity.getUid());
        ActivityUserVO userVO = new ActivityUserVO(
                user.getUid(),
                user.getUsername(),
                user.getAvatarUrl()
        );
        List<String> images = List.of();
        try {
            images = objectMapper.readValue(activity.getImages(), new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            log.error("解析图片JSON时出现错误", e);
            throw new ServiceException(500, "内部服务错误，请稍后再试");
        }
        if (activity.getActivityType() == 1) {
            VideoPostDTO videoPost = videoFeignClient.getVideoPost(activity.getObjectId(), viewerUserId);
            return new ActivityVO(activity, userVO, videoPost);
        }
        if (activity.getRefNkid() != 0L) {
            VideoPostDTO videoPost = videoFeignClient.getVideoPost(activity.getRefNkid(), viewerUserId);
            ActivityVO activityVO = new ActivityVO(activity, userVO, images);
            activityVO.setRefVideoPost(videoPost);
            return activityVO;
        }
        return new ActivityVO(activity, userVO, images);
    }
}
