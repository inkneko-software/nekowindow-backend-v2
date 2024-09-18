package com.inkneko.nekowindow.video.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PartitionInfo {

    @TableId
    Integer partitionId;
    String partitionName;
    String description;
}
