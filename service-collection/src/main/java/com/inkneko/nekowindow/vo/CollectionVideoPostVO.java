package com.inkneko.nekowindow.vo;

import com.inkneko.nekowindow.entity.Collection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionVideoPostVO {
    private Long nkid;
    private String title;
    private String coverUrl;
    private Integer duration;
    private Long visit;
    private Long danmakuNums;
    private String uploaderName;
    private String createdAt;

    public CollectionVideoPostVO(Collection collection, String title, String coverUrl, Integer duration,
                                 Long visit, Long danmakuNums, String uploaderName) {
        this.nkid = collection.getNkid();
        this.title = title;
        this.coverUrl = coverUrl;
        this.duration = duration;
        this.visit = visit;
        this.danmakuNums = danmakuNums;
        this.uploaderName = uploaderName;
        this.createdAt = collection.getCreatedAt();
    }
}
