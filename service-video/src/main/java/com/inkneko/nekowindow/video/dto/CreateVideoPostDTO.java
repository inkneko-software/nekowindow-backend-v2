package com.inkneko.nekowindow.video.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateVideoPostDTO {
    @Size(min = 1, max = 200, message = "标题长度不符合要求，请设置在1到200个字符之间")
    String title;

    @Size(max = 900, message = "标题长度最大为900个字符")
    String description;

    @NotBlank
    String coverUrl;

    @NotNull
    Integer partitionId;

    @NotNull
    List<String> tags;

    @NotBlank
    String videoUrl;
}
