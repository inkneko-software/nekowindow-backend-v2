package com.inkneko.nekowindow.video.permission.policy;

import com.inkneko.nekowindow.video.permission.scene.VideoPostResourceAccessScene;
import com.inkneko.nekowindow.video.permission.state.VideoPostResourceState;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class VideoPostResourceVisibilityPolicy {
    public static Set<Integer> visibleStates(VideoPostResourceAccessScene scene) {
        return switch (scene) {
            case PUBLIC -> Set.of(VideoPostResourceState.NORMAL.getCode());
            case OWNER -> Set.of(
                    VideoPostResourceState.NORMAL.getCode(),
                    VideoPostResourceState.REVIEWING.getCode(),
                    VideoPostResourceState.REVIEW_FAILED.getCode(),
                    VideoPostResourceState.SELF_HIDDEN.getCode()
            );
            case ADMIN -> Arrays.stream(VideoPostResourceState.values())
                    .map(VideoPostResourceState::getCode)
                    .collect(Collectors.toSet());
        };
    }
}
