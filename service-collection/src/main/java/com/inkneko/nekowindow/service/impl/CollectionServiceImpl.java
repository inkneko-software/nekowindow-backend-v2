package com.inkneko.nekowindow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.inkneko.nekowindow.api.user.client.UserFeignClient;
import com.inkneko.nekowindow.api.user.vo.UserVo;
import com.inkneko.nekowindow.api.video.client.VideoFeignClient;
import com.inkneko.nekowindow.api.video.dto.VideoPostDTO;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.entity.Collection;
import com.inkneko.nekowindow.entity.CollectionGroup;
import com.inkneko.nekowindow.mapper.CollectionGroupMapper;
import com.inkneko.nekowindow.mapper.CollectionMapper;
import com.inkneko.nekowindow.service.CollectionService;
import com.inkneko.nekowindow.vo.CollectionGroupVO;
import com.inkneko.nekowindow.vo.CollectionVideoPostVO;
import jakarta.validation.constraints.NotNull;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CollectionServiceImpl implements CollectionService {

    CollectionMapper collectionMapper;
    CollectionGroupMapper collectionGroupMapper;

    VideoFeignClient videoFeignClient;
    UserFeignClient userFeignClient;

    public CollectionServiceImpl(
            CollectionMapper collectionMapper,
            CollectionGroupMapper collectionGroupMapper,
            VideoFeignClient videoFeignClient,
            UserFeignClient userFeignClient) {
        this.collectionMapper = collectionMapper;
        this.collectionGroupMapper = collectionGroupMapper;
        this.videoFeignClient = videoFeignClient;
        this.userFeignClient = userFeignClient;
    }

    @Override
    public CollectionGroupVO createCollectionGroup(Long userId, String name, String description) {
        CollectionGroup collectionGroup = collectionGroupMapper.selectOne(new LambdaQueryWrapper<CollectionGroup>().eq(CollectionGroup::getUid, userId).eq(CollectionGroup::getName, name));
        if (collectionGroup != null) {
            throw new ServiceException(400, "收藏夹名称已存在");
        }
        collectionGroup = new CollectionGroup();
        collectionGroup.setUid(userId);
        collectionGroup.setName(name);
        collectionGroup.setDescription(description);
        collectionGroupMapper.insert(collectionGroup);
        collectionGroup = collectionGroupMapper.selectById(collectionGroup.getGroupId());
        return new CollectionGroupVO(collectionGroup, "", 0L);
    }

    @Override
    public void updateCollectionGroup(Long groupId, String name, String description, Long userId) {
        CollectionGroup collectionGroup = collectionGroupMapper.selectById(groupId);
        if (collectionGroup == null || collectionGroup.getState().equals(CollectionGroup.State.DELETED.getValue())) {
            throw new ServiceException(404, "收藏夹不存在");
        }
        if (!collectionGroup.getUid().equals(userId)) {
            throw new ServiceException(403, "无权修改该收藏夹");
        }
        collectionGroupMapper.update(
                null,
                Wrappers.<CollectionGroup>lambdaUpdate()
                        .set(CollectionGroup::getName, name)
                        .set(CollectionGroup::getDescription, description)
                        .eq(CollectionGroup::getGroupId, groupId)
        );
    }

    @Override
    public void removeCollectionGroup(Long groupId, Long userId) {
        CollectionGroup collectionGroup = collectionGroupMapper.selectById(groupId);
        if (collectionGroup == null || collectionGroup.getState().equals(CollectionGroup.State.DELETED.getValue())) {
            throw new ServiceException(404, "收藏夹不存在");
        }
        if (!collectionGroup.getUid().equals(userId)) {
            throw new ServiceException(403, "无权删除该收藏夹");
        }

        collectionGroupMapper.update(
                null,
                Wrappers.<CollectionGroup>lambdaUpdate()
                        .set(CollectionGroup::getState, CollectionGroup.State.DELETED.getValue())
                        .eq(CollectionGroup::getGroupId, groupId)
        );
    }

    @Override
    public CollectionGroupVO getCollectionGroupByGroupId(Long groupId, Long viewerUserId) {
        CollectionGroup collectionGroup = collectionGroupMapper.selectById(groupId);
        if (collectionGroup == null || collectionGroup.getState().equals(CollectionGroup.State.DELETED.getValue())) {
            throw new ServiceException(404, "收藏夹不存在");
        }

        if (!collectionGroup.getUid().equals(viewerUserId) && collectionGroup.getState().equals(CollectionGroup.State.PRIVATE.getValue())) {
            throw new ServiceException(403, "无权访问该收藏夹");
        }

        String previewCoverUrl = "";
        Collection collection = collectionMapper.selectOne(new LambdaQueryWrapper<Collection>().eq(Collection::getGroupId, collectionGroup.getGroupId()).last("LIMIT 1"));
        if (collection != null) {
            VideoPostDTO videoPostDTO = videoFeignClient.getVideoPost(collection.getNkid(), viewerUserId);
            if (videoPostDTO != null) {
                previewCoverUrl = videoPostDTO.getCoverUrl();
            }
        }
        Long collectionCount = collectionMapper.selectCount(new LambdaQueryWrapper<Collection>().eq(Collection::getGroupId, collectionGroup.getGroupId()));
        return new CollectionGroupVO(collectionGroup, previewCoverUrl, collectionCount);
    }

    @Override
    public List<CollectionGroupVO> getCollectionGroupsByUserId(@NotNull Long userId, Long viewerUserId) {
        LambdaQueryWrapper<CollectionGroup> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollectionGroup::getUid, userId).ne(CollectionGroup::getState, CollectionGroup.State.DELETED.getValue());
        // 如果查看者不是收藏夹所有者，则只显示公开的收藏夹
        if (!userId.equals(viewerUserId)){
            wrapper.eq(CollectionGroup::getState, CollectionGroup.State.PUBLIC.getValue());
        }

        List<CollectionGroup> collectionGroups = collectionGroupMapper.selectList(wrapper);

        List<CollectionGroupVO> collectionGroupVOs = new ArrayList<>();
        for (CollectionGroup group : collectionGroups) {
            Collection collection = collectionMapper.selectOne(new LambdaQueryWrapper<Collection>().eq(Collection::getGroupId, group.getGroupId()).last("LIMIT 1"));
            String previewCoverUrl = "";
            if (collection != null) {
                VideoPostDTO videoPostDTO = videoFeignClient.getVideoPost(collection.getNkid(), viewerUserId);
                if (videoPostDTO != null) {
                    previewCoverUrl = videoPostDTO.getCoverUrl();
                }
            }

            collectionGroupVOs.add(new CollectionGroupVO(
                    group,
                    previewCoverUrl,
                    collectionMapper.selectCount(new LambdaQueryWrapper<Collection>().eq(Collection::getGroupId, group.getGroupId()))
            ));

        }

            return collectionGroupVOs;
    }

    @Override
    public void addCollection(Long groupId, Long nkid, Long userId) {
        CollectionGroup collectionGroup = collectionGroupMapper.selectById(groupId);
        if (collectionGroup == null || collectionGroup.getState().equals(CollectionGroup.State.DELETED.getValue())) {
            throw new ServiceException(404, "收藏夹不存在");
        }
        if (!collectionGroup.getUid().equals(userId)) {
            throw new ServiceException(403, "无权操作该收藏夹");
        }
        Collection collection = collectionMapper.selectOne(new LambdaQueryWrapper<Collection>().eq(Collection::getGroupId, groupId).eq(Collection::getNkid, nkid));
        if (collection != null) {
            throw new ServiceException(400, "已收藏该视频");
        }
        collection = new Collection();
        collection.setGroupId(groupId);
        collection.setNkid(nkid);
        try{
            collectionMapper.insert(collection);
        }catch (DataIntegrityViolationException e){
            throw new ServiceException(400, "已收藏该视频");
        }
    }

    @Override
    public void removeCollection(Long groupId, Long nkid, Long userId) {
        CollectionGroup collectionGroup = collectionGroupMapper.selectById(groupId);
        if (collectionGroup == null || collectionGroup.getState().equals(CollectionGroup.State.DELETED.getValue())) {
            throw new ServiceException(404, "收藏夹不存在");
        }
        if (!collectionGroup.getUid().equals(userId)) {
            throw new ServiceException(403, "无权操作该收藏夹");
        }
        Collection collection = collectionMapper.selectOne(new LambdaQueryWrapper<Collection>().eq(Collection::getGroupId, groupId).eq(Collection::getNkid, nkid));
        if (collection == null) {
            throw new ServiceException(404, "收藏不存在");
        }
        collectionMapper.delete(
                new LambdaQueryWrapper<Collection>()
                        .eq(Collection::getGroupId, groupId)
                        .eq(Collection::getNkid, nkid)
        );
    }

    @Override
    public List<CollectionVideoPostVO> getCollections(Long groupId, Long viewerUserId, long page, long pageSize) {
        CollectionGroup collectionGroup = collectionGroupMapper.selectById(groupId);
        if (collectionGroup == null || collectionGroup.getState().equals(CollectionGroup.State.DELETED.getValue())) {
            throw new ServiceException(404, "收藏夹不存在");
        }
        if (!collectionGroup.getUid().equals(viewerUserId) && collectionGroup.getState().equals(CollectionGroup.State.PRIVATE.getValue())) {
            throw new ServiceException(403, "无权访问该收藏夹");
        }

        List<Collection> collections = collectionMapper.selectPage(
                new Page<>(page, pageSize),
                new LambdaQueryWrapper<Collection>().eq(Collection::getGroupId, groupId)
        ).getRecords();
        List<Long> nkidList = new ArrayList<>();
        for (Collection collection : collections) {
            nkidList.add(collection.getNkid());
        }
        List<CollectionVideoPostVO> collectionVideoPostVOs = new ArrayList<>();
        if (nkidList.isEmpty()) {
            return collectionVideoPostVOs;
        }
        Map<Long, VideoPostDTO> videoPostDTOMap = videoFeignClient.getVideoPostBatch(nkidList, viewerUserId);

        for (Collection collection : collections) {
            VideoPostDTO videoPostDTO = videoPostDTOMap.get(collection.getNkid());
            if (videoPostDTO != null) { // 仅返回对当前用户可见的稿件
                UserVo uploader = userFeignClient.get(videoPostDTO.getUid());
                collectionVideoPostVOs.add(new CollectionVideoPostVO(
                        collection,
                        videoPostDTO.getTitle(),
                        videoPostDTO.getCoverUrl(),
                        videoPostDTO.getDuration(),
                        videoPostDTO.getVisit(),
                        0L,
                        uploader.getUsername()
                ));
            } else {
                collectionVideoPostVOs.add(null);
            }
        }

        return collectionVideoPostVOs;
    }
}
