package com.inkneko.nekowindow.controller;

import com.inkneko.nekowindow.common.Response;
import com.inkneko.nekowindow.common.util.GatewayAuthUtils;
import com.inkneko.nekowindow.dto.SendDanmakuDTO;
import com.inkneko.nekowindow.entity.ChatMessage;
import com.inkneko.nekowindow.entity.ChatRoom;
import com.inkneko.nekowindow.service.DanmakuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/danmaku")
public class DanmakuController {
    @Autowired
    DanmakuService danmakuService;

    @PostMapping("/sendDanmaku")
    public Response<ChatMessage> sendDanmaku(@RequestBody SendDanmakuDTO dto){
        Long uid = GatewayAuthUtils.auth();
        ChatMessage chatMessage = new ChatMessage(
                null,
                dto.getChatRoomId(),
                dto.getTime(),
                uid,
                dto.getContent(),
                dto.getType(),
                dto.getColor(),null
        );
        danmakuService.saveDanmaku(chatMessage);
        return new Response<ChatMessage>("发送成功", chatMessage);
    }

    @GetMapping("/getChatRoom")
    public Response<ChatRoom> getChatRoom(@RequestParam Long chatRoomId){
        ChatRoom chatRoom = danmakuService.getChatRoom(chatRoomId);
        return new Response<>("ok", chatRoom);
    }

    @GetMapping("/getChatRoomByVideoResourceId")
    public Response<ChatRoom> getChatRoomByVideoResourceId(@RequestParam Long videoResourceId){
        ChatRoom chatRoom = danmakuService.getChatRoomByVideoResourceId(videoResourceId);
        return new Response<>("ok", chatRoom);
    }

    @GetMapping("/getRecentDanmakuList")
    public Response<List<ChatMessage>> getRecentDanmakuList(@RequestParam Long chatRoomId, @RequestParam(defaultValue = "1500") int n){
        java.util.List<ChatMessage> chatMessageList = danmakuService.getRecentDanmakuList(chatRoomId, n);
        return new Response<>("ok", chatMessageList);
    }
}
