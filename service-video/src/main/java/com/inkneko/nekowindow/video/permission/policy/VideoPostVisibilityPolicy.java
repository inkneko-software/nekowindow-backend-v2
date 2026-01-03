package com.inkneko.nekowindow.video.permission.policy;

import com.inkneko.nekowindow.video.permission.state.VideoPostState;
import com.inkneko.nekowindow.video.permission.scene.VideoPostAccessScene;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public final class VideoPostVisibilityPolicy {

    public static Set<Integer> visibleStates(VideoPostAccessScene scene) {
        return switch (scene) {
            case PUBLIC -> Set.of(VideoPostState.NORMAL.getCode());
            case OWNER -> Set.of(
                    VideoPostState.NORMAL.getCode(),
                    VideoPostState.REVIEWING.getCode(),
                    VideoPostState.REVIEW_FAILED.getCode(),
                    VideoPostState.SELF_HIDDEN.getCode()
            );
            case ADMIN -> Arrays.stream(VideoPostState.values())
                    .map(VideoPostState::getCode)
                    .collect(Collectors.toSet());
        };
    }
}
