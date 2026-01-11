package com.inkneko.nekowindow.service;

import com.inkneko.nekowindow.entity.Collection;
import com.inkneko.nekowindow.entity.CollectionGroup;
import com.inkneko.nekowindow.vo.CollectionGroupVO;
import com.inkneko.nekowindow.vo.CollectionVideoPostVO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CollectionService {

    /**
     * 创建收藏夹
     *
     * @param userId 用户ID
     * @param name 收藏夹名称
     * @param description 收藏夹描述
     * @return 收藏夹
     */
    CollectionGroupVO createCollectionGroup(Long userId, String name, String description);

    /**
     * 更新收藏夹信息
     *
     * @param groupId 收藏夹ID
     * @param name 收藏夹名称
     * @param description 收藏夹描述
     * @param userId 操作者用户ID
     */
    void updateCollectionGroup(Long groupId, String name, String description, Long userId);

    /**
     * 删除收藏夹
     *
     * @param groupId 收藏夹ID
     * @param userId 操作者用户ID
     */
    void removeCollectionGroup(Long groupId, Long userId);

    /**
     * 通过收藏夹ID获取收藏夹
     *
     * @param groupId 收藏夹ID
     * @param viewerUserId 查看者用户ID
     * @return 收藏夹
     */
    CollectionGroupVO getCollectionGroupByGroupId(Long groupId, Long viewerUserId);

    /**
     * 获取用户收藏夹列表
     *
     * @param userId 用户ID
     * @param viewerUserId 查看者用户ID
     * @return 收藏夹列表
     */
    List<CollectionGroupVO> getCollectionGroupsByUserId(Long userId, Long viewerUserId);

    /**
     * 添加收藏
     * @param groupId 收藏夹ID
     * @param nkid 视频投稿ID
     * @param userId 操作者用户ID
     */
    void addCollection(Long groupId, Long nkid, Long userId);

    /**
     * 移除收藏
     * @param groupId 收藏夹ID
     * @param nkid 视频投稿ID
     * @param userId 操作者用户ID
     */
    void removeCollection(Long groupId, Long nkid, Long userId);

    /**
     * 获取收藏夹内的所有收藏稿件ID
     * @param groupId 收藏夹ID
     * @param viewerUserId 查看者用户ID
     * @return 稿件ID列表
     */
    List<CollectionVideoPostVO> getCollections(Long groupId, Long viewerUserId, long page, long pageSize);
}
