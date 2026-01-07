收藏服务（collection），提供视频收藏功能
---

- 收藏分组的创建与查询
- 将视频保存至收藏分组

## 数据库定义

数据库 nekowindow_collection

```sql
create database nekowindow_collection;
use nekowindow_collection;
```



### 收藏分组表

```sql
CREATE TABLE collection_group(
    group_id BIGINT UNSIGNED PRIMARY KEY AUTO_INCREMENT,
    uid BIGINT UNSIGNED NOT NULL COMMENT '所有者uid',
    name VARCHAR(30) NOT NULL COMMENT '收藏夹名称',
    description VARCHAR(255) NOT NULL COMMENT '收藏夹描述',
    state TINYINT NOT NULL DEFAULT 0 COMMENT '收藏夹状态，0正常，1非公开, 2删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏夹创建时间'
)ENGINE=InnoDB DEFAULT CHARSET utf8mb4;
```

### 视频收藏


```sql
CREATE TABLE collection(
    group_id BIGINT UNSIGNED NOT NULL COMMENT '所属分组id',
    nkid BIGINT UNSIGNED NOT NULL COMMENT '稿件id',
    is_deleted TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除，0正常，1删除',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '收藏创建时间',
    PRIMARY KEY(group_id, nkid)
)ENGINE=InnoDB DEFAULT CHARSET utf8mb4;
```

