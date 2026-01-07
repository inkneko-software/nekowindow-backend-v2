package com.inkneko.nekowindow.api.video.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoPostDTO {
    Long nkid;
    Long uid;
    String title;
    String coverUrl;
    Integer duration;
    Integer shared;
    String description;
    Integer partitionId;
    String partitionName;
    Date createdAt;

}
