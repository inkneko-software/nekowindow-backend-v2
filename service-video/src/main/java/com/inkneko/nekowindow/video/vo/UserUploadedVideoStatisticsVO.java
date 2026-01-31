package com.inkneko.nekowindow.video.vo;

import com.inkneko.nekowindow.video.entity.VideoPost;
import com.inkneko.nekowindow.video.entity.VideoPostResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

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
    Long visit;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long likes;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Long coin;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    Integer shared;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    String description;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<String> tags;

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

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    List<UserUploadedVideoPostResourceVO> uploadedVideoPostResourceVOS;

    public UserUploadedVideoStatisticsVO(VideoPost videoPost, List<String> tags, List<VideoPostResource> videoPostResources){
        this.nkid = videoPost.getNkid();
        this.title = videoPost.getTitle();
        this.coverUrl = videoPost.getCoverUrl();
        this.duration = videoPost.getDuration();
        this.visit = videoPost.getVisit();
        this.shared = videoPost.getShared();
        this.likes = videoPost.getLikes();
        this.coin = videoPost.getCoin();
        this.description = videoPost.getDescription();
        this.state = videoPost.getState();
        this.tags = tags;
        this.partitionId = videoPost.getPartitionId();
        this.partitionName = videoPost.getPartitionName();
        this.reviewFailedReason = videoPost.getReviewFailedReason();
        this.createdAt = videoPost.getCreatedAt();
        this.uploadedVideoPostResourceVOS = videoPostResources.stream().map(UserUploadedVideoPostResourceVO::new).toList();

    }
}
