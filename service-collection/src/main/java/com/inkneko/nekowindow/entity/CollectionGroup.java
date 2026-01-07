package com.inkneko.nekowindow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionGroup {
    /**
     * CREATE TABLE collection_groups(
     *     group_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
     *     uid BIGINT UNSIGNED NOT NULL COMMENT '所有者uid',
     *     name VARCHAR(30) NOT NULL COMMENT '收藏夹名称',
     *     description VARCHAR(255) NOT NULL COMMENT '收藏夹描述',
     *     state TINYINT NOT NULL DEFAULT 0 COMMENT '收藏夹状态，0正常，1非公开, 2删除',
     *     created_at DATETIME NOT NULL COMMENT '收藏夹创建时间',
     *
     * )ENGINE=InnoDB DEFAULT CHARSET utf8mb4;
     */

    @TableId(type = IdType.AUTO)
    private Long groupId;
    private Long uid;
    private String name;
    private String description;
    private Integer state;
    private String createdAt;

    @Getter
    @AllArgsConstructor
    public enum State {
        PUBLIC(0),
        PRIVATE(1),
        DELETED(2);

        private final int value;
    }

}
