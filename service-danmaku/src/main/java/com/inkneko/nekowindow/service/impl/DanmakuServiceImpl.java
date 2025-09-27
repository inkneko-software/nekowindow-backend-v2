package com.inkneko.nekowindow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.entity.ChatMessage;
import com.inkneko.nekowindow.entity.ChatRoom;
import com.inkneko.nekowindow.mapper.ChatMessageMapper;
import com.inkneko.nekowindow.mapper.ChatRoomMapper;
import com.inkneko.nekowindow.service.DanmakuService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DanmakuServiceImpl implements DanmakuService {

    ChatRoomMapper chatRoomMapper;
    ChatMessageMapper chatMessageMapper;

    public DanmakuServiceImpl(ChatRoomMapper chatRoomMapper, ChatMessageMapper chatMessageMapper) {
        this.chatRoomMapper = chatRoomMapper;
        this.chatMessageMapper = chatMessageMapper;
    }

    @Override
    public ChatRoom getChatRoom(Long chatRoomId) {
        return chatRoomMapper.selectById(chatRoomId);
    }

    @Override
    public ChatRoom getChatRoomByVideoResourceId(Long videoResourceId) {
        ChatRoom chatRoom = chatRoomMapper.selectOne(new LambdaQueryWrapper<ChatRoom>()
                .eq(ChatRoom::getVideoId, videoResourceId));
        if (chatRoom == null) {
            chatRoom = new ChatRoom();
            chatRoom.setVideoId(videoResourceId);
            chatRoomMapper.insert(chatRoom);
        }
        return chatRoom;
    }

    @Override
    public List<ChatMessage> getRecentDanmakuList(Long chatRoomId, int n) {
        Page<ChatMessage> chatMessagePage = chatMessageMapper.selectPage(new Page<>(1, n), new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getChatId, chatRoomId)
                .orderByDesc(ChatMessage::getCreatedAt));
        return chatMessagePage.getRecords();

    }

    @Override
    public Long saveDanmaku(ChatMessage chatMessage) {
        ChatRoom chatRoom = chatRoomMapper.selectById(chatMessage.getChatId());
        if (chatRoom == null) {
            throw new ServiceException(1400, "指定ChatRoomId不存在");
        }

        chatMessage.setMessageId(null);
        chatMessage.setCreatedAt(null);
        chatMessageMapper.insert(chatMessage);
        return chatMessage.getMessageId();
    }

    @Override
    public void removeDanmaku(Long messageId) {
        chatMessageMapper.deleteById(messageId);
    }

    @Override
    public List<ChatMessage> getDanmakuListByUser(Long uid, int page, int n) {
        Page<ChatMessage> chatMessagePage = chatMessageMapper.selectPage(new Page<>(page, n), new LambdaQueryWrapper<ChatMessage>()
                .eq(ChatMessage::getUserId, uid)
                .orderByDesc(ChatMessage::getCreatedAt));
        return chatMessagePage.getRecords();
    }
}
