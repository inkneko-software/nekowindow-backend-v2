package com.inkneko.nekowindow.user.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GetUserFollowerListDTO {
    @NotNull
    private Long uid;

    @Min(value = 1, message = "页码不能小于1")
    private Integer page = 1;

    @Min(value = 1, message = "页大小不能小于1")
    @Max(value = 30, message = "页大小不能大于30")
    private Integer pageSize = 20;
}
