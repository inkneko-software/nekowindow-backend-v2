package com.inkneko.nekowindow.api.mq.video;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoPostCreatedDTO {
    private Long nkid;
    private Long userId;
    private String title;
    private String description;
    private String coverUrl;
    private Date createdAt;
}
