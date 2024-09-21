package com.inkneko.nekowindow.video.vo;

import com.inkneko.nekowindow.video.entity.VideoPost;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUploadedVideoStatisticsVO {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long nkid;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String title;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String coverUrl;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer duration;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer shared;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String description;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer state;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer partitionId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String partitionName;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String reviewFailedReason;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Date createdAt;

    public UserUploadedVideoStatisticsVO(VideoPost videoPost){
        this.nkid = videoPost.getNkid();
        this.title = videoPost.getTitle();
        this.coverUrl = videoPost.getCoverUrl();
        this.duration = videoPost.getDuration();
        this.shared = videoPost.getShared();
        this.description = videoPost.getDescription();
        this.state = videoPost.getState();
        this.partitionId = videoPost.getPartitionId();
        this.partitionName = videoPost.getPartitionName();
        this.reviewFailedReason = videoPost.getReviewFailedReason();
        this.createdAt = videoPost.getCreatedAt();
    }
}
