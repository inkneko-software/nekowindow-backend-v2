package com.inkneko.nekowindow.controller;

import com.inkneko.nekowindow.common.Response;
import com.inkneko.nekowindow.common.util.GatewayAuthUtils;
import com.inkneko.nekowindow.service.CollectionService;
import com.inkneko.nekowindow.vo.CollectionGroupVO;
import com.inkneko.nekowindow.vo.CollectionVideoPostVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/collection/")
public class CollectionController {

    CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @PostMapping("/createCollectionGroup")
    @Operation(summary = "创建收藏夹")
    public Response<CollectionGroupVO> createCollectionGroup(@RequestParam String name, @RequestParam(defaultValue = "") String description) {
        Long userId = GatewayAuthUtils.auth(true);
        CollectionGroupVO collectionGroupVO = collectionService.createCollectionGroup(userId, name, description);
        return new Response<>("创建成功", collectionGroupVO);
    }

    @GetMapping("/getCollectionGroups")
    @Operation(summary = "获取用户收藏夹列表")
    public Response<List<CollectionGroupVO>> getCollectionGroups(@RequestParam(required = false) Long userId/*, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = '') Integer pageSize*/) {
        // 如果userId为空，则获取当前登录用户的收藏夹
        Long currentUserId = null;
        if (userId == null) {
            currentUserId = GatewayAuthUtils.auth(true);
            userId = currentUserId;
        } else {
            currentUserId = GatewayAuthUtils.auth(false);
        }
        return new Response<>("ok", collectionService.getCollectionGroupsByUserId(userId, currentUserId));
    }

    @PostMapping("/deleteCollectionGroup")
    @Operation(summary = "删除收藏夹")
    public Response<Boolean> deleteCollectionGroup(@RequestParam Long collectionGroupId) {
        Long userId = GatewayAuthUtils.auth();
        collectionService.removeCollectionGroup(collectionGroupId, userId);
        return new Response<>("删除成功");
    }

    @PostMapping("/updateCollectionGroup")
    @Operation(summary = "更新收藏夹信息")
    public Response<CollectionGroupVO> updateCollectionGroup(@RequestParam Long collectionGroupId, @RequestParam String name, @RequestParam String description) {
        Long userId = GatewayAuthUtils.auth();
        collectionService.updateCollectionGroup(collectionGroupId, name, description, userId);
        return new Response<>("更新成功");
    }

    @GetMapping("/getCollectionVideoPosts")
    @Operation(summary = "获取收藏夹内的视频列表")
    public Response<List<CollectionVideoPostVO>> getCollectionVideoPosts(@RequestParam Long collectionGroupId, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "36") Integer pageSize) {
        Long userId = GatewayAuthUtils.auth(false);
        return new Response<>("ok", collectionService.getCollections(collectionGroupId, userId, page, pageSize));
    }

    @PostMapping("/addCollectionVideoPost")
    @Operation(summary = "添加收藏")
    public Response<?> addCollectionVideoPost(@RequestParam Long collectionGroupId, @RequestParam Long nkid) {
        Long userId = GatewayAuthUtils.auth();
        collectionService.addCollection(collectionGroupId, nkid, userId);
        return new Response<>("收藏成功");
    }

    @PostMapping("/removeCollectionVideoPost")
    @Operation(summary = "移除收藏")
    public Response<?> removeCollectionVideoPost(@RequestParam Long collectionGroupId, @RequestParam Long nkid) {
        Long userId = GatewayAuthUtils.auth();
        collectionService.removeCollection(collectionGroupId, nkid, userId);
        return new Response<>("移除成功");
    }
}
