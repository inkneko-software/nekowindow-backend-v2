package com.inkneko.nekowindow.video.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoCoinRecord {
    private Long id;
    private Long uid;
    private Long nkid;
    private Integer num;
    private Long orderId;
    private String createdAt;
}
