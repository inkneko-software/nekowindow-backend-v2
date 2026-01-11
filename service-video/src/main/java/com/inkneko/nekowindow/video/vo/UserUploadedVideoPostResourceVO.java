package com.inkneko.nekowindow.video.vo;

import com.inkneko.nekowindow.video.entity.VideoPostResource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUploadedVideoPostResourceVO {
    Long videoId;
    Integer duration;
    Integer state;
    Long visit;
    String reviewFailedReason;
    Integer conversionState;
    String conversionFailedReason;
    String dashMpdUrl;
    LocalDateTime createdAt;

    public UserUploadedVideoPostResourceVO(VideoPostResource videoPostResource){
        this.videoId = videoPostResource.getVideoId();
        this.duration = videoPostResource.getDuration();
        this.state = videoPostResource.getState();
        this.visit = videoPostResource.getVisit();
        this.reviewFailedReason = videoPostResource.getReviewFailedReason();
        this.conversionState = videoPostResource.getConvertState();
        this.conversionFailedReason = videoPostResource.getConvertErrMsg();
        this.dashMpdUrl = videoPostResource.getDashMpdUrl();
        this.createdAt = videoPostResource.getCreatedAt();
    }
}
