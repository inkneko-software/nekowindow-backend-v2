package com.inkneko.nekowindow.video.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoHelloWorldCardVO {
    private Long nkid;
    private String title;
    private String coverUrl;
    private Date createdAt;
}
