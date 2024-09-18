package com.inkneko.nekowindow.api.oss.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenUploadUrlDTO {
    String bucket;
    String objectKey;
}
