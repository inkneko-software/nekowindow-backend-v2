package com.inkneko.nekowindow.vo;

import com.inkneko.nekowindow.api.video.dto.VideoPostDTO;
import com.inkneko.nekowindow.entity.Activity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityVO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private ActivityUserVO user;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer activityType;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long objectId;
    private VideoPostDTO videoPost;
    private VideoPostDTO refVideoPost;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> images = List.of();
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer likes;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Date createdAt;

    public ActivityVO(Activity activity, ActivityUserVO user, List<String> images){
        this.id = activity.getId();
        this.user = user;
        this.title = activity.getTitle();
        this.content = activity.getContent();
        this.activityType = activity.getActivityType();
        this.objectId = activity.getObjectId();
        this.images = images;
        this.likes = activity.getLikes();
        this.createdAt = activity.getCreatedAt();
        this.videoPost = null;
        this.refVideoPost = null;
    }

    public ActivityVO(Activity activity, ActivityUserVO user, VideoPostDTO videoPost){
        this.id = activity.getId();
        this.user = user;
        this.title = activity.getTitle();
        this.content = activity.getContent();
        this.activityType = activity.getActivityType();
        this.objectId = activity.getObjectId();
        this.images = List.of();
        this.likes = activity.getLikes();
        this.createdAt = activity.getCreatedAt();
        this.videoPost = videoPost;
    }

}
