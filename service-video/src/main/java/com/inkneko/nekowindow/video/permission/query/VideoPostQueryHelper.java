package com.inkneko.nekowindow.video.permission.query;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.inkneko.nekowindow.video.entity.VideoPost;
import com.inkneko.nekowindow.video.permission.policy.VideoPostVisibilityPolicy;
import com.inkneko.nekowindow.video.permission.scene.VideoPostAccessScene;

public class VideoPostQueryHelper {

    public static LambdaQueryWrapper<VideoPost> visibleWrapper(
            VideoPostAccessScene scene,
            Long uid
    ) {
        LambdaQueryWrapper<VideoPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(VideoPost::getState,
                VideoPostVisibilityPolicy.visibleStates(scene));

        if (scene == VideoPostAccessScene.OWNER) {
            wrapper.eq(VideoPost::getUid, uid);
        }
        return wrapper;
    }
}
