package com.inkneko.nekowindow.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Activity {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long uid;
    private String title;
    private String content;
    private Integer activityType;
    private Long objectId;
    private Long refNkid;
    private String images;
    private Integer state;
    private Integer likes;
    private Date createdAt;
}
