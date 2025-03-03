package com.inkneko.nekowindow.encode.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AudioEncodeParameter {
    String maxBitRate;
    Integer audioQualityCode;
    String codec;
}