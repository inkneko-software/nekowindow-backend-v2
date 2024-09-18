package com.inkneko.nekowindow.encode.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EncodeParameter {
    String width;
    String height;
    String bitRate;
    String codec;
    String codecTag;
    String frameRate;
    String videoCrf;
    String videoKeyFrameMinInterval;
    String videoGopSize;
    String videoScaleOption;

}
