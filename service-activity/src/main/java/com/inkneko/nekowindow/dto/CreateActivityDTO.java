package com.inkneko.nekowindow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@Data
public class CreateActivityDTO {

    @NotNull(message = "动态标题不能为空")
    @Size(max = 20, message = "动态标题不能超过20个字符")
    @Schema(description = "动态标题")
    private String title;

    @NotBlank
    @Size(max = 1000, message = "动态内容不能超过1000个字符")
    @Schema(description = "动态内容")
    private String content;

    @Schema(description = "引用的视频id")
    private Long refNkid = 0L;

    @Schema(description = "动态图片")
    @Size(max = 9, message = "动态图片不能超过9张")
    private List<String> images = List.of();

    @Schema(description = "动态是否公开")
    private Boolean isPublic = true;
}
