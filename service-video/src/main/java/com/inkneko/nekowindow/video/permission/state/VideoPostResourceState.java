package com.inkneko.nekowindow.video.permission.state;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 视频状态，0 正常访问，1 违规删除，2 自主删除，3 审核中，4 审核未通过, 5 投稿者自主隐藏
 */
@Getter
@AllArgsConstructor
public enum VideoPostResourceState {
    NORMAL(0),
    VIOLATION_DELETE(1),
    SELF_DELETE(2),
    REVIEWING(3),
    REVIEW_FAILED(4),
    SELF_HIDDEN(5);

    private final int code;
}