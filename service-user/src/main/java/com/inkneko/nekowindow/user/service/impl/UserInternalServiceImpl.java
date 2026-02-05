package com.inkneko.nekowindow.user.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.inkneko.nekowindow.api.user.vo.UserVo;
import com.inkneko.nekowindow.user.entity.Relation;
import com.inkneko.nekowindow.user.entity.UserDetail;
import com.inkneko.nekowindow.user.mapper.RelationMapper;
import com.inkneko.nekowindow.user.mapper.UserDetailMapper;
import com.inkneko.nekowindow.user.service.UserInternalService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserInternalServiceImpl implements UserInternalService {


    private final RelationMapper relationMapper;
    private final UserDetailMapper userDetailMapper;

    public UserInternalServiceImpl(RelationMapper relationMapper, UserDetailMapper userDetailMapper) {
        this.relationMapper = relationMapper;
        this.userDetailMapper = userDetailMapper;
    }

    @Override
    public List<UserVo> getUserSubscribeList(Long userId) {
        List<Relation> relations = relationMapper.selectList(
                Wrappers.<Relation>lambdaQuery()
                        .eq(Relation::getFollowerUid, userId)
        );

        if (relations.isEmpty()) {
            return List.of();
        }

        List<Long> targetUidList = relations.stream().map(Relation::getTargetUid).toList();
        List<UserDetail> userDetails = userDetailMapper.selectBatchIds(targetUidList);
        return userDetails.stream().map(
                userDetail -> new UserVo(
                        userDetail.getUid(),
                        userDetail.getUsername(),
                        userDetail.getSign(),
                        userDetail.getExp(),
                        userDetail.getGender(),
                        userDetail.getBirth(),
                        userDetail.getAvatarUrl(),
                        userDetail.getBannerUrl(),
                        userDetail.getFans(),
                        userDetail.getSubscribes()
                )
        ).toList();
    }

    @Override
    public List<UserVo> getUserFansList(Long userId) {
        List<Relation> relations = relationMapper.selectList(
                Wrappers.<Relation>lambdaQuery()
                        .eq(Relation::getTargetUid, userId)
        );
        List<Long> targetUidList = relations.stream().map(Relation::getTargetUid).toList();
        List<UserDetail> userDetails = userDetailMapper.selectBatchIds(targetUidList);
        return userDetails.stream().map(
                userDetail -> new UserVo(
                        userDetail.getUid(),
                        userDetail.getUsername(),
                        userDetail.getSign(),
                        userDetail.getExp(),
                        userDetail.getGender(),
                        userDetail.getBirth(),
                        userDetail.getAvatarUrl(),
                        userDetail.getBannerUrl(),
                        userDetail.getFans(),
                        userDetail.getSubscribes()
                )
        ).toList();
    }
}
