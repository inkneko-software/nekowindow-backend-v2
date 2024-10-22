package com.inkneko.nekowindow.video.vo;

import com.inkneko.nekowindow.video.entity.VideoPost;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoPostDetailVO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long nkid;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String coverUrl;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private UploadUserVO uploader;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<VideoPostResourceVO> videos;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> tags;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Date createdAt;

    public VideoPostDetailVO(VideoPost videoPost, UploadUserVO uploader, List<VideoPostResourceVO> videos, List<String> tags) {
        this.nkid = videoPost.getNkid();
        this.title = videoPost.getTitle();
        this.description = videoPost.getDescription();
        this.coverUrl = videoPost.getCoverUrl();
        this.createdAt = videoPost.getCreatedAt();
        this.uploader = uploader;  // 由外部提供的 UploadUserVO 对象
        this.videos = videos;      // 由外部提供的视频资源列表
        this.tags = tags;          // 由外部提供的标签列表
    }

}
