package com.inkneko.nekowindow.encode.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoEncodeParameter {
    String maxBitRate;
    Integer videoQualityCode;
    String codec;
    String frameRate;
    String videoCrf;
    Integer videoGopSize;
    String videoScaleOption;
    Integer height;

}