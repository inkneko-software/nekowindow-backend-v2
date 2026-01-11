package com.inkneko.nekowindow.vo;

import com.inkneko.nekowindow.entity.Collection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionVideoPostVO {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long nkid;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String coverUrl;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer duration;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long visit;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long danmakuNums;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String uploaderName;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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
