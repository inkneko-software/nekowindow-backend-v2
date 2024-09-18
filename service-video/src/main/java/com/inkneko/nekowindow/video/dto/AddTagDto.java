package com.inkneko.nekowindow.video.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddTagDto {
    @NotNull(message = "分区id不能为空")
    private Integer partitionId;

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 20, message = "标签名称过长")
    private String name;
}
