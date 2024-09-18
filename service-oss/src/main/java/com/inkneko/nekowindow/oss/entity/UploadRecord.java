package com.inkneko.nekowindow.oss.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;


@Data
public class UploadRecord {
    @TableId(type = IdType.AUTO)
    private Long ossId;
    private Long uid;
    private String endpoint;
    private String bucket;
    private String objectKey;
    private String url;
}