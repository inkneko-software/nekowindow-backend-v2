package com.inkneko.nekowindow.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Collection {
    /**
     * CREATE TABLE collection(
     *     group_id BIGINT UNSIGNED NOT NULL COMMENT '所属分组id',
     *     nkid BIGINT UNSIGNED NOT NULL COMMENT '稿件id',
     *     is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除，0正常，1删除',
     *     created_at DATETIME NOT NULL COMMENT '收藏创建时间',
     *     PRIMARY KEY(group_id, nkid)
     * )ENGINE=InnoDB DEFAULT CHARSET utf8mb4;
     */

    private Long groupId;
    private Long nkid;
    private Integer isDeleted;
    private String createdAt;
}
