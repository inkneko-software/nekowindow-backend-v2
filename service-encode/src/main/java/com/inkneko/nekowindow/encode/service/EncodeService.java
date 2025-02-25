package com.inkneko.nekowindow.encode.service;


import com.inkneko.nekowindow.encode.dto.ProbeRequestDTO;
import com.inkneko.nekowindow.encode.entity.AudioEncodeTask;
import com.inkneko.nekowindow.encode.entity.ProbeResult;
import com.inkneko.nekowindow.encode.entity.VideoEncodeTask;
import com.inkneko.nekowindow.encode.util.MediaUtils;

import java.io.IOException;
import java.util.List;

public interface EncodeService {

    /**
     * 分析指定视频资源的视频流与音频流，并调用视频服务，更新视频资源信息
     *
     * @param dto 请求参数
     * @return 分析结果，见 {@code ProbeResult}
     * @throws IOException          网络异常
     * @throws InterruptedException 中断异常
     */
    ProbeResult probeVideo(ProbeRequestDTO dto) throws IOException, InterruptedException;

    /**
     * 保存视频转码任务
     *
     * @param videoEncodeTask 任务
     */
    void saveVideoEncodeTask(VideoEncodeTask videoEncodeTask);

    /**
     * 更新指定视频编码任务为已完成
     *
     * @param videoId 视频ID
     * @param segmentIndex 分段编号
     * @param qualityCode 视频质量代码
     */
    void updateVideoEncodeTaskComplete(Long videoId, Integer segmentIndex, Integer qualityCode, String resultVideoUrl);

    /**
     * 保存音频转码任务
     *
     * @param audioEncodeTask 任务
     */
    void saveAudioEncodeTask(AudioEncodeTask audioEncodeTask);

    /**
     * 更新音频编码任务为已完成
     *
     * @param videoId 视频ID
     * @param qualityCode 音频质量代码
     */
    void updateAudioEncodeTaskComplete(Long videoId, Integer qualityCode, String resultAudioUrl);

    /**
     * 查询指定视频是否完成视频与音频转码。用于检查是否可以开始合并生成DASH描述文件
     * <p>
     * 用户应当使用锁机制，来避免并发冲突
     *
     * @param videoId 视频ID
     *
     * @return 是否完成转码
     */
    boolean isVideoEncodeTaskCompleted(Long videoId);

    /**
     * 查询指定视频ID的视频转码任务
     * @param videoId 视频ID
     * @return 转码任务列表
     */
    List<VideoEncodeTask> getVideoEncodeTasks(Long videoId);

    /**
     * 查询指定视频ID的音频转码任务
     * @param videoId
     * @return 转码任务列表
     */
    List<AudioEncodeTask> getAudioEncodeTasks(Long videoId);



}
