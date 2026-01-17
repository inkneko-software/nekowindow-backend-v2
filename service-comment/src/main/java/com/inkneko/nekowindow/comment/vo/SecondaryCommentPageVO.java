package com.inkneko.nekowindow.comment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SecondaryCommentPageVO {
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long total;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long page;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private Long size;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private List<SecondaryCommentVO> secondaryComments;
}
