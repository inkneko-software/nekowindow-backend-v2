package com.inkneko.nekowindow.video.dto;

import lombok.Data;

import java.util.List;

/**
 * 更新投稿信息。对于不进行更新的字段，设置为null
 */
@Data
public class UpdatePostBriefDTO {
    private Long nkid;
    private String title;
    private String description;
    private String coverUrl;
    private String videoUrl;
    private List<String> tags;
    private Boolean isPostPublic;
}
