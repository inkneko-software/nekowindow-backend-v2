package com.inkneko.nekowindow.service;

import com.inkneko.nekowindow.dto.CreateActivityDTO;
import com.inkneko.nekowindow.dto.UpdateActivityDTO;
import com.inkneko.nekowindow.entity.Activity;
import com.inkneko.nekowindow.vo.ActivityVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
public interface ActivityService {
    /**
     * 保存动态
     *
     * @param dto 动态
     * @param userId 用户id
     * @return 动态
     */
    ActivityVO saveActivity(CreateActivityDTO dto, Long userId);

    /**
     * 保存视频投稿动态
     *
     * @param nkid 视频id
     * @param userId 用户id
     * @return 动态
     */
    ActivityVO saveVideoPostActivity(Long nkid, Long userId);

    /**
     * 通过id获取动态
     *
     * @param id 动态id
     * @return 动态
     */
    ActivityVO getActivityById(Long id, Long viewerUserId);

    /**
     * 删除动态
     *
     * @param id 动态id
     */
    void deleteActivity(Long id, Long userId);

    /**
     * 更新动态
     *
     * @param dto 动态
     * @param userId 操作者用户id
     */
    void updateActivity(UpdateActivityDTO dto, Long userId);

    /**
     * 通过用户的动态
     *
     * @param userId 用户id
     * @param viewerUserId 访问者用户id
     * @param lastActivityId 最后一个动态id，为空则默认查询最新动态
     * @param size 获取的动态数量
     * @return 动态列表
     */
    List<ActivityVO> getUserActivities(@NotNull Long userId, Long viewerUserId, Long lastActivityId, @Min(1) @Max(20) long size);

    /**
     * 获取用户的时间线
     *
     * @param userId 用户id
     * @param lastActivityId 最后一个动态id
     * @param size 获取的动态数量
     * @return 动态列表
     */
    List<ActivityVO> getUserTimeLine(@NotNull Long userId, Long lastActivityId, @Min(1) @Max(20) long size);
}
