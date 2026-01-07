package com.inkneko.nekowindow.api.video.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetVideoPostBatchDTO {

    private List<Long> nkidList;
    private Long viewerUserId;
}
