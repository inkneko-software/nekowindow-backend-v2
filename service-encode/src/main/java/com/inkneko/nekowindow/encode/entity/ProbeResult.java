package com.inkneko.nekowindow.encode.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProbeResult {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Stream {
        /**
         *      * type StreamInfo struct {
         *      * 	CodecType  string `json:"codec_type"`   //流类型 video/audio
         *      * 	CodecName  string `json:"codec_name"`   //编码类型 h264/aac
         *      * 	RFrameRate string `json:"r_frame_rate"` //视频专有，帧率，如"24000/1001"，约为24帧
         *      * 	Width      int    `json:"width"`        //视频专有，为视频宽度
         *      * 	Height     int    `json:"height"`       //视频专有，为视频高度
         *      * 	BitRate    string `json:"bit_rate"`     //码率，有可能为空
         *      *
         *      * }
         */
        @JsonProperty("codec_type")
        private String codecType;

        @JsonProperty("codec_name")
        private String codecName;

        @JsonProperty("r_frame_rate")
        private String rFrameRate;

        @JsonProperty("width")
        private Integer width;

        @JsonProperty("height")
        private Integer height;

        @JsonProperty("bit_rate")
        private String bitRate;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Format {
        /**
         *      * type FormatInfo struct {
         *      * 	Duration   string `json:"duration"`
         *      * 	Size       string `json:"size"`
         *      * 	ProbeScore int32  `json:"probe_score"`
         *      * 	BitRate    string `json:"bit_rate"` //文件总码率
         *      * }
         */

        @JsonProperty("duration")
        private String duration;

        @JsonProperty("size")
        private String size;

        @JsonProperty("probe_score")
        private Integer probeScore;

        @JsonProperty("bit_rate")
        private String bitRate;
    }

    /**
     *      * type ProbeOutput struct {
     *      * 	Streams []StreamInfo `json:"streams"`
     *      * 	Format  FormatInfo   `json:"format"`
     *      * }
     */

    @JsonProperty("streams")
    private List<Stream> streams;

    @JsonProperty("format")
    private Format format;

}
