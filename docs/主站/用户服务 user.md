用户服务 (user)
---

提供用户基础功能，以及用户之间的社交功能，包括关注、私信等功能

- [ ] 用户间关注关系的管理
  - [ ] 用户关注，取消关注
- [ ] 用户间私信功能
- [ ] 个人资料查询与编辑
  - 昵称
  - 个性签名
  - 头像
  - 性别
  - 生日

## 数据库定义

```sql
CREATE DATABASE nekowindow_account;
USE nekowindow_account;
```


### 用户认证信息表
用于存储鉴权信息，包括密码的哈希值，登录用的邮箱

```sql
CREATE TABLE user_credential(
    uid BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户id',
    auth_salt CHAR(64)  NOT NULL COMMENT '盐, md5(uuid.NewString())',
    auth_hash CHAR(64) NOT NULL COMMENT 'sha1(auth_salt + password)',
    email VARCHAR(255)  COMMENT '邮箱',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE(email)
)ENGINE=InnoDB DEFAULT CHARSET utf8mb4;
```

### 用户详细信息表

```sql
CREATE TABLE user_detail(
	uid BIGINT PRIMARY KEY,
    username VARCHAR(20) NOT NULL COMMENT '用户名，用作唯一标识，默认值式为neko_{uid}，不可手动指定为其他uid',
    sign VARCHAR(255) NOT NULL DEFAULT '' COMMENT '个性签名',
    exp INT NOT NULL DEFAULT 0 COMMENT '经验',
    gender VARCHAR(255) NOT NULL DEFAULT '保密' COMMENT '性别， 可选值 男，女，保密',
    birth DATE COMMENT '生日，MM-dd',
    avatar_url VARCHAR(255) NOT NULL DEFAULT '/images/avatar/default.jpg' COMMENT '头像地址',
    banner_url VARCHAR(255) NOT NULL DEFAULT '/images/banner/default.jpg' COMMENT '空间封面图片地址',
    fans INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '粉丝数量',
    subscribes INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '关注数量',
    coins INT NOT NULL DEFAULT 0 COMMENT '硬币数',
    UNIQUE(username)
)ENGINE=InnoDB DEFAULT CHARSET utf8mb4;
```

### 每日登录奖励记录表

```sql
CREATE TABLE daily_bonus_record(
	uid BIGINT NOT NULL,
    coins INT NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX(uid)
)ENGINE=InnoDB DEFAULT CHARSET utf8mb4;
```

### 用户关系表

```sql
CREATE TABLE relation(
    target_uid BIGINT COMMENT '被关注者的uid',
    follower_uid BIGINT COMMENT '关注者的uid',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '关注时间',
    PRIMARY KEY(target_uid, follower_uid)
)ENGINE=InnoDB DEFAULT CHARSET utf8mb4;
```

### 私信表

```sql
CREATE TABLE private_message(
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT,
    target_uid BIGINT NOT NULL COMMENT '接收者uid',
    sender_uid BIGINT NOT NULL COMMENT '发送者uid',
    target_read BOOL NOT NULL DEFAULT FALSE COMMENT '接收者是否已读',
    content VARCHAR(500) NOT NULL DEFAULT '' COMMENT '内容',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
)ENGINE=InnoDB DEFAULT CHARSET utf8mb4;
```