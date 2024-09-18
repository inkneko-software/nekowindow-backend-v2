package com.inkneko.nekowindow.video.vo;

import com.inkneko.nekowindow.video.entity.PartitionInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HomeRecommendVO {

    @Schema(description = "海报列表")
    List<PosterVO> posters;

    @Schema(description = "全站视频推荐")
    List<VideoPostBriefVO> recommendVideos;

    @Schema(description = "热销商品列表")
    List<String> popularMerchants;

    @Schema(description = "分区列表")
    List<PartitionInfo> partitions;

    @Schema(description = "对应分区的热门视频推荐列表（每个分区最多10个推荐视频）")
    List<List<VideoPostBriefVO>> partitionVideos;
}
