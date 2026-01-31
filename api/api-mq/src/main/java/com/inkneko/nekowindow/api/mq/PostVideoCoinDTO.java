package com.inkneko.nekowindow.api.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostVideoCoinDTO {
    private Long orderId;
    private Long nkid;
    private Long userId;
    private Integer coinNum;
}
