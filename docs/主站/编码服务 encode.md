编码服务（encode）
---
提供音视频转码功能

### 错误处理
对于编码服务，因为工作流基于消息队列，导致错误处理上有一些特殊的考虑

对于服务的实例来讲，如果遇到需要下线的情况，而刚好获取了一个转码消息，那么在服务下线的时候会先终止ffmpeg进程，但目前没有好的办法去区分究竟是转码过程出现错误(比如代码8输入文件不存在)，还是被父进程终止(进程返回值255，但该值不确定，暂时没有看ffmpeg源代码是如何执行的)

### 并发处理

由于一个视频分段为多个消息进行转码，在全部分段转码完成后需要进行合并。在判断是否完成转码时需要进行加锁，避免重复生产合并消息

锁名称：`EncodeService.CheckConcatTaskLock.%d`

格式化数据为：`video_id`
### 视频质量代号


| 视频质量 | 代号 | 说明 |
|-|-|-|
|自动|0| 客户端专用，指定0为自动码率选择|
|1080P 60帧| 10 | 原视频帧率>=60帧。转码后码率不高于8Mbps |
|1080P 高码率| 11 | 原视频帧率小于60帧，但码率大于3Mbps。转码后码率不高于8Mbps |
|1080P| 12 | 最高码率3Mbps，帧率不超过59帧 |
|720P| 20 | 最高码率1Mbps |
|360P | 30 | 最高码率500Kbps |

### 音频质量代号
| 音频质量 | 代号 | 说明 |
|-|-|-|
| 320K | 70 | 最高码率320kbps|
| 128K | 71 | 最高码率128kbps|




## 数据库定义

数据库 nekowindow_encode

```sql
CREATE DATABASE nekowindow_encode;
use nekowindow_encode;
```
### 视频转码任务表

记录视频分片的转码任务进度

```sql
CREATE TABLE video_encode_task(
    video_id BIGINT NOT NULL COMMENT '视频id',
    video_quality_code INT NOT NULL COMMENT '视频质量代号，见表格',
    source_video_url VARCHAR(255) NOT NULL COMMENT '原视频地址',
    result_video_url VARCHAR(255) COMMENT '转码完成的视频地址，为NULL则为未完成',
    segment_index INT NOT NULL COMMENT '分片编号，起始为0',
    segment_total INT NOT NULL COMMENT '总分片数',
    segment_size VARCHAR(255) NOT NULL COMMENT '分片时长，以秒为单位，针对NTSC帧率可为小数',
    target_codec VARCHAR(255) NOT NULL COMMENT '目标编码格式，如h264',
    target_bitrate VARCHAR(255) NOT NULL COMMENT '目标码率，如8M，500K',
    target_height INT NOT NULL COMMENT '目标高度，如720',
    target_frame_rate VARCHAR(255) NOT NULL COMMENT '',
    encode_failed_reason VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '任务创建时间',
    started_at DATETIME COMMENT '开始转码时间，NULL为未开始',
    complete_at DATETIME COMMENT '完成转码时间，NULL为未完成',
    PRIMARY KEY(video_id, video_quality_code, segment_index)
)ENGINE=InnoDB DEFAULT CHARSET utf8mb4;
```

### 音频转码任务表

记录音频的转码任务进度

```sql
CREATE TABLE audio_encode_task(
    video_id BIGINT NOT NULL,
    source_video_url VARCHAR(255) NOT NULL COMMENT '原视频地址',
    result_audio_url VARCHAR(255) COMMENT '转码完成的音频地址，为NULL则为未完成',
    audio_quality_code INT NOT NULL COMMENT '音频质量代号，见表格',
    target_codec VARCHAR(255) NOT NULL COMMENT '目标编码格式',
    target_bitrate VARCHAR(255) NOT NULL COMMENT '目标码率',
    encode_failed_reason VARCHAR(1024) NOT NULL DEFAULT '' COMMENT '编码失败原因',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '任务创建时间',
    started_at DATETIME COMMENT '任务开始时间',
    complete_at DATETIME COMMENT '任务完成时间',
    PRIMARY KEY(video_id, audio_quality_code)
)ENGINE=InnoDB DEFAULT CHARSET utf8mb4;
```

### 视频合并任务表

记录视频合并生成DASH文件的任务进度

```sql
CREATE TABLE dash_concat_task(
    video_id BIGINT NOT NULL PRIMARY KEY,
    result_dash_mpd_file
)ENGINE=InnoDB DEFAULT CHARSET utf8mb4;
```