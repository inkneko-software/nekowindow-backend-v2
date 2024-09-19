package com.inkneko.nekowindow.video.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoPostBriefVO {
    private Long nkid;
    private String title;
    private String description;
    private UploadUserVO uploader;
    private List<String> tags;
    private Date createdAt;
}
