package com.inkneko.nekowindow.api.oss.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadRecordVO {
    private Long ossId;
    private Long uid;
    private String endpoint;
    private String bucket;
    private String objectKey;
}
