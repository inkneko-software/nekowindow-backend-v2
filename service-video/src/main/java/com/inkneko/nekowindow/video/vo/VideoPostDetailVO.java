package com.inkneko.nekowindow.video.vo;

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
}
