package com.inkneko.nekowindow.vo;

import com.inkneko.nekowindow.entity.CollectionGroup;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionGroupVO {
    @Schema(description = "分组ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long groupId;

    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long uid;

    @Schema(description = "分组名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "分组描述", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(description = "状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer state;

    @Schema(description = "预览封面URL，如果无有效视频，为空串", requiredMode = Schema.RequiredMode.REQUIRED)
    private String previewCoverUrl;

    @Schema(description = "收藏数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long collectionCount;

    @Schema(description = "创建时间", requiredMode = Schema.RequiredMode.REQUIRED)
    private String createdAt;

    public CollectionGroupVO(CollectionGroup collectionGroup, String previewCoverUrl, Long collectionCount) {
        this.groupId = collectionGroup.getGroupId();
        this.uid = collectionGroup.getUid();
        this.name = collectionGroup.getName();
        this.description = collectionGroup.getDescription();
        this.state = collectionGroup.getState();
        this.previewCoverUrl = previewCoverUrl;
        this.collectionCount = collectionCount;
        this.createdAt = collectionGroup.getCreatedAt();
    }
}
