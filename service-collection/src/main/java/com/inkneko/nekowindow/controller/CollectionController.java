package com.inkneko.nekowindow.controller;

import com.inkneko.nekowindow.common.Response;
import com.inkneko.nekowindow.common.util.GatewayAuthUtils;
import com.inkneko.nekowindow.service.CollectionService;
import com.inkneko.nekowindow.vo.CollectionGroupVO;
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
    public Response<CollectionGroupVO> createCollectionGroup(@RequestParam String name, @RequestParam(defaultValue = "") String description) {
        Long userId = GatewayAuthUtils.auth(true);
        CollectionGroupVO collectionGroupVO = collectionService.createCollectionGroup(userId, name, description);
        return new Response<>("创建成功", collectionGroupVO);
    }

    @GetMapping("/getCollectionGroups")
    public Response<List<CollectionGroupVO>> getCollectionGroups(@RequestParam(required = false) Long userId/*, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = '') Integer pageSize*/) {
        Long currentUserId = null;
        if (userId == null) {
            currentUserId = GatewayAuthUtils.auth(true);
        } else {
            currentUserId = GatewayAuthUtils.auth(false);
        }
        userId = currentUserId;
        return new Response<>("ok", collectionService.getCollectionGroupsByUserId(userId, currentUserId));
    }

}
