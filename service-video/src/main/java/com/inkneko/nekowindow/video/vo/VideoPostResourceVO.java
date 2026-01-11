package com.inkneko.nekowindow.video.vo;

import com.inkneko.nekowindow.video.entity.VideoPostResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoPostResourceVO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long videoId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long visit;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String dashMpdUrl;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String videoAdaptions;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String audioAdaptions;


    public VideoPostResourceVO(VideoPostResource videoPostResource) {
        this.videoId = videoPostResource.getVideoId();
        this.title = videoPostResource.getTitle();
        this.visit = videoPostResource.getVisit();
        this.dashMpdUrl = videoPostResource.getDashMpdUrl();
        this.videoAdaptions = videoPostResource.getVideoAdaptions();
        this.audioAdaptions = videoPostResource.getAudioAdaptions();
    }
}
