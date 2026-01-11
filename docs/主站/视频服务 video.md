视频服务（video）
---

提供视频投递，视频管理，视频数据统计等功能

- [ ] 投稿功能 
  - [x] 由video-fs服务提供文件上传功能，获得上传vid
  - [x] 由picture-fs服务提供封面上传的功能，获得封面图片url
  - [x] 使用本人上传的视频资源vid，封面url，稿件标题、标题、简介、分区id，标签进行稿件的创建
    - [ ] 多part上传

- [ ] 稿件管理
  - [ ] 用户可查询自己上传的稿件
    - [ ] 查询视频基础信息
    - [ ] 查询视频转码信息

- [ ] 稿件信息普通用户的查询
- [ ] 播放数据的统计，包括用户年龄，弹幕热度，每日播放量
- [ ] 视频的简易推荐，基于分区，基于标签

## 数据库定义

数据库 nekowindow_video

```sql
CREATE DATABASE nekowindow_video;
use nekowindow_video;
```

### 稿件信息表

投稿信息

```sql
CREATE TABLE video_post(
    nkid BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '稿件id',
    uid BIGINT NOT NULL COMMENT '上传者id',
    title VARCHAR(255) NOT NULL COMMENT '稿件标题',
    cover_url VARCHAR(255) NOT NULL COMMENT '视频封面URL',
    duration INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '视频总时长',
    visit BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '播放数量',
    shared INT UNSIGNED NOT NULL DEFAULT 0 COMMENT '转发数量',
    description VARCHAR(1000) NOT NULL COMMENT '视频简介',
    state TINYINT NOT NULL DEFAULT 3 COMMENT '稿件状态，0 正常访问，1 违规删除，2 自主删除，3 审核中，4 审核未通过, 5 投稿者自主隐藏',
    partition_id INT UNSIGNED NOT NULL COMMENT '分区id',
    partition_name VARCHAR(255) NOT NULL COMMENT '分区名称',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '稿件创建时间',
    review_failed_reason VARCHAR(1024) NOT NULL DEFAULT '',
    INDEX(uid)
)ENGINE=InnoDB DEFAULT CHARSET utf8mb4;
```

### 稿件视频表

投稿下的视频信息

关于视频转码，为了能提供较好的网络适配性，

如果视频分辨率小于640x360，则最大分辨率显示为360p，提供[流畅360p 200Kbps码率@30fps]

如果视频分辨率小于1280x720，则最大分辨率显示为720p, 提供[高清720p 视频原画质码率], [流畅360p 200Kbps码率@30fps]

如果视频分辨率大于1280x720, 则最大分辨率显示为1080p，提供[超清1080p 原画质码率];若视频帧数超过60帧，则提供[超清1080p 2000Kbps @30fps], [超清1080p60 原画质码率], [高清720p 780Kbps @30fps]

```sql
CREATE TABLE video_post_resource(
    video_id BIGINT NOT NULL PRIMARY KEY AUTO_INCREMENT COMMENT '视频id',
    nkid BIGINT NOT NULL COMMENT '稿件id',
    title VARCHAR(255) NOT NULL COMMENT '视频标题',
    visit BIGINT UNSIGNED NOT NULL COMMENT '播放数量',
    state INT NOT NULL  DEFAULT 3 COMMENT '视频状态，0 正常访问，1 违规删除，2 自主删除，3 审核中，4 审核未通过, 5 投稿者自主隐藏',
    review_failed_reason VARCHAR(1024),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    convert_state INT NOT NULL COMMENT '转码状态，0未开始任务，1正在转码，2正在合并, 3转码完成，4转码失败',
    convert_err_msg VARCHAR(255) NOT NULL DEFAULT '' COMMENT '转码失败消息',
    duration INT NOT NULL DEFAULT 0 COMMENT '视频时长，以秒为单位，非精准数据',
    source_video_url VARCHAR(255) NOT NULL COMMENT '原视频',
    dash_mpd_url VARCHAR(255) NOT NULL COMMENT 'mpd文件',
    conversion_at TIMESTAMP COMMENT '开始转码时间',
    video_adaptions VARCHAR(255) NOT NULL DEFAULT '' COMMENT '可选视频质量代码，以逗号为分隔',
    audio_adaptions VARCHAR(255) NOT NULL DEFAULT '' COMMENT '可选音频质量代码，以逗号为分隔'
    UNIQUE(video_id, nkid)
)ENGINE=InnoDB DEFAULT CHARSET utf8mb4;
```

### 视频分片编码信息表

存储转码消息

更新本分段的状态 --> select for update video_resources查询转码状态，若状态位正在转码则查询本表是否完成，若全部完成则发送合并，更新转码状态
```sql
CREATE TABLE video_encode_tasks(
	video_id BIGINT NOT NULL,
    segment_index INT NOT NULL,
    state INT NOT NULL DEFAULT 0 COMMENT '处理进度，0为未编排，1为已编排，2为转码失败，3为转码完成',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY(video_id, segment_index)
)Engine=InnoDB DEFAULT CHARSET utf8mb4;
```



### 分区信息表

分区信息

```sql
CREATE TABLE partition_info(
    partition_id INT PRIMARY KEY COMMENT '分区id',
    partition_name VARCHAR(255) NOT NULL COMMENT '分区名称',
    description VARCHAR(255)
)ENGINE=InnoDB DEFAULT CHARSET utf8mb4;

# 分区：
INSERT INTO partition_info VALUES
    (1, "知识", ""),
    (2, "影视", ""),
    (3, "生活", ""),
    (4, "动画", ""),
    (5, "游戏", "");
```

### 稿件标签表

```sql
CREATE TABLE post_tag(
  nkid BIGINT NOT NULL COMMENT '所属稿件',
  tag_name VARCHAR(255) COMMENT  '标签名称',
  INDEX(nkid),
  UNIQUE(nkid, tag_name)
)ENGINE=InnoDB DEFAULT CHARSET utf8mb4;
```

### 分区标签推荐表

仅做视频上传页面展示使用

```sql
CREATE TABLE partition_recommend_tag(
  partition_id INT UNSIGNED NOT NULL COMMENT '所属分区id',
  tag_name VARCHAR(20) NOT NULL COMMENT '标签名称'
)ENGINE=InnoDB DEFAULT CHARSET utf8mb4;
```

初始数据

```sql
INSERT INTO partition_recommend_tag (partition_id, tag_name) values
(1, "数学"),
(1, "英语"),
(1, "高中"),
(1, "大学"),
(1, "计算机科学"),
(1, "Java"),
(1, "Golang"),
(1, "Python"),
(1, "C++"),
(1, "计算机网络"),
(2, "纪录片"),
(2, "电影"),
(2, "电视剧"),
(3, "美食"),
(3, "健身"),
(5, "Minecraft"),
(5, "Earth:Online");
```
