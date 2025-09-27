package com.inkneko.nekowindow.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.inkneko.nekowindow.entity.ChatMessage;
import com.inkneko.nekowindow.entity.ChatRoom;

import java.util.List;

public interface DanmakuService {


    /**
     * 获取聊天室信息
     * @param chatRoomId 聊天室ID
     * @return 聊天室信息
     */
    ChatRoom getChatRoom(Long chatRoomId);

    /**
     * 通过视频资源ID获取对应的聊天室
     * @param videoResourceId 视频资源ID
     * @return 对应的聊天室信息，若对应视频资源没有对应的聊天室ID，则自动生成一个聊天室ID并返回
     */
    ChatRoom getChatRoomByVideoResourceId(Long videoResourceId);
    
    /**
     * 获取指定聊天室的最近弹幕消息列表
     *
     * @param chatRoomId 聊天室ID
     * @param n 最近消息数量
     * @return 弹幕消息列表
     */
    List<ChatMessage> getRecentDanmakuList(Long chatRoomId, int n);

    /**
     * 保存弹幕消息
     *
     * @param chatMessage 弹幕消息
     * @return 保存后的消息ID
     */
    Long saveDanmaku(ChatMessage chatMessage);

    /**
     * 删除指定ID的弹幕消息
     *
     * @param messageId 消息ID
     */
    void removeDanmaku(Long messageId);

    /**
     * 根据用户ID获取该用户发送的弹幕消息列表，支持分页
     *
     * @param uid 用户ID
     * @param page 页数，从1开始
     * @param n 每页数量
     * @return 弹幕消息列表
     */
    List<ChatMessage> getDanmakuListByUser(Long uid, int page, int n);
}
