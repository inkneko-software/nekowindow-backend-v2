package com.inkneko.nekowindow.encode.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProbeRequestDTO {
    private Long videoId;
    private String sourceVideoUrl;
}
