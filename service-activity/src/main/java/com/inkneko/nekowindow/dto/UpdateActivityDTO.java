package com.inkneko.nekowindow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdateActivityDTO {

    @NotNull
    @Schema(description = "动态id")
    private  Long id;

    @Schema(description = "动态标题")
    private String title;

    @Schema(description = "动态内容")
    private String content;

    @Schema(description = "动态图片")
    @Size(max = 9)
    private List<String> images;

    @Schema(description = "动态是否公开")
    private Boolean isPublic;
}
