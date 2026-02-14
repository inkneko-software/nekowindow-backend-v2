package com.inkneko.nekowindow.controller;

import com.inkneko.nekowindow.common.Response;
import com.inkneko.nekowindow.common.util.GatewayAuthUtils;
import com.inkneko.nekowindow.dto.CreateActivityDTO;
import com.inkneko.nekowindow.dto.UpdateActivityDTO;
import com.inkneko.nekowindow.service.ActivityService;
import com.inkneko.nekowindow.service.impl.ActivityServiceImpl;
import com.inkneko.nekowindow.vo.ActivityVO;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/activity")
public class ActivityController {

    private final ActivityService activityService;

    public ActivityController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @PostMapping("/createActivity")
    public Response<ActivityVO> createActivity(@Validated @RequestBody CreateActivityDTO dto) {
        Long userId = GatewayAuthUtils.auth();
        return new Response<>("创建成功", activityService.saveActivity(dto, userId));
    }

    @GetMapping("/getActivityById")
    public Response<ActivityVO> getActivityById(@RequestParam Long id) {
        Long userId = GatewayAuthUtils.auth(false);
        return new Response<>("ok", activityService.getActivityById(id, userId));
    }

    @PostMapping("/updateActivity")
    public Response<ActivityVO> updateActivity(@Validated @RequestBody UpdateActivityDTO dto) {
        Long userId = GatewayAuthUtils.auth();
        activityService.updateActivity(dto, userId);
        return new Response<>("更新成功");
    }

    @PostMapping("/deleteActivity")
    public Response<ActivityVO> deleteActivity(@RequestParam Long id) {
        Long userId = GatewayAuthUtils.auth();
        activityService.deleteActivity(id, userId);
        return new Response<>("删除成功");
    }

    @GetMapping("/getUserActivities")
    public Response<List<ActivityVO>> getUserActivities(@RequestParam Long userId, @RequestParam(required = false) Long lastActivityId, @RequestParam(defaultValue = "20") @Max(20) @Min(1) long size) {
        Long viewerUserId = GatewayAuthUtils.auth(false);
        return new Response<>("ok", activityService.getUserActivities(userId, viewerUserId, lastActivityId, size));
    }

    @GetMapping("/getUserTimeLine")
    public Response<List<ActivityVO>> getUserTimeLine(@RequestParam(required = false) Long lastActivityId, @RequestParam(defaultValue = "20") @Max(20) @Min(1) long size) {
        Long userId = GatewayAuthUtils.auth();
        return new Response<>("ok", activityService.getUserTimeLine(userId, lastActivityId, size));
    }
}
