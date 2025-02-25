package com.inkneko.nekowindow.encode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkneko.nekowindow.api.video.dto.UpdateVideoResourceConversionStateDTO;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.common.util.OssUtils;
import com.inkneko.nekowindow.encode.config.EncodeConfig;
import com.inkneko.nekowindow.encode.dto.ProbeRequestDTO;
import com.inkneko.nekowindow.encode.entity.AudioEncodeTask;
import com.inkneko.nekowindow.encode.entity.ProbeResult;
import com.inkneko.nekowindow.encode.entity.VideoEncodeTask;
import com.inkneko.nekowindow.encode.mapper.AudioEncodeTaskMapper;
import com.inkneko.nekowindow.encode.mapper.VideoEncodeTaskMapper;
import com.inkneko.nekowindow.encode.service.EncodeService;
import com.inkneko.nekowindow.encode.util.MediaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;

import java.io.*;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class EncodeServiceImpl implements EncodeService {

    S3Client s3Client;
    RabbitTemplate rabbitTemplate;
    VideoEncodeTaskMapper videoEncodeTaskMapper;
    AudioEncodeTaskMapper audioEncodeTaskMapper;

    public EncodeServiceImpl(S3Client s3Client,
                             RabbitTemplate rabbitTemplate,
                             VideoEncodeTaskMapper videoEncodeTaskMapper,
                             AudioEncodeTaskMapper audioEncodeTaskMapper) {
        this.s3Client = s3Client;
        this.rabbitTemplate = rabbitTemplate;
        this.videoEncodeTaskMapper = videoEncodeTaskMapper;
        this.audioEncodeTaskMapper = audioEncodeTaskMapper;
    }

    @Override
    public ProbeResult probeVideo(ProbeRequestDTO dto) throws IOException, InterruptedException, ServiceException {
        ObjectMapper objectMapper = new ObjectMapper();
        //查询视频链接，并下载视频文件
        OssUtils.OssLink ossLink = OssUtils.url(dto.getSourceVideoUrl());
        if (ossLink == null) {
            throw new ServiceException(400, "视频链接格式错误");
        }
        File tempFile = new File(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        s3Client.getObject(request -> request.bucket(ossLink.bucket).key(ossLink.key), Path.of(tempFile.toURI()));

        return MediaUtils.probeVideo(tempFile);
    }

    @Override
    public void saveVideoEncodeTask(VideoEncodeTask videoEncodeTask) {
        videoEncodeTaskMapper.insert(videoEncodeTask);
    }

    @Override
    public void saveAudioEncodeTask(AudioEncodeTask audioEncodeTask) {
        audioEncodeTaskMapper.insert(audioEncodeTask);
    }

    @Override
    public boolean isVideoEncodeTaskCompleted(Long videoId) {
        List<VideoEncodeTask> videoEncodeTasks = videoEncodeTaskMapper.selectList(new LambdaQueryWrapper<VideoEncodeTask>().eq(VideoEncodeTask::getVideoId, videoId));
        List<AudioEncodeTask> audioEncodeTask = audioEncodeTaskMapper.selectList(new LambdaQueryWrapper<AudioEncodeTask>().eq(AudioEncodeTask::getVideoId, videoId));
        boolean completed = true;
        for (VideoEncodeTask videoEncodeTask : videoEncodeTasks) {
            if (videoEncodeTask.getCompleteAt() == null){
                completed = false;
                break;
            }
        }

        for (AudioEncodeTask videoEncodeTask : audioEncodeTask) {
            if (videoEncodeTask.getCompleteAt() == null){
                completed = false;
                break;
            }
        }
        return completed;
    }

    @Override
    public void updateVideoEncodeTaskComplete(Long videoId, Integer segmentIndex, Integer qualityCode, String resultVideoUrl) {
        VideoEncodeTask videoEncodeTask = videoEncodeTaskMapper.selectOne(
                new LambdaQueryWrapper<VideoEncodeTask>()
                        .eq(VideoEncodeTask::getVideoId, videoId)
                        .eq(VideoEncodeTask::getSegmentIndex, segmentIndex)
                        .eq(VideoEncodeTask::getVideoQualityCode, qualityCode)
        );
        videoEncodeTask.setCompleteAt(LocalDateTime.now());
        videoEncodeTask.setResultVideoUrl(resultVideoUrl);
        videoEncodeTaskMapper.update(
                videoEncodeTask,
                new LambdaQueryWrapper<VideoEncodeTask>()
                        .eq(VideoEncodeTask::getVideoId, videoId)
                        .eq(VideoEncodeTask::getSegmentIndex, segmentIndex)
                        .eq(VideoEncodeTask::getVideoQualityCode, qualityCode)
        );
    }

    @Override
    public void updateAudioEncodeTaskComplete(Long videoId, Integer qualityCode, String resultAudioUrl) {
        AudioEncodeTask audioEncodeTask = audioEncodeTaskMapper.selectOne(
                new LambdaQueryWrapper<AudioEncodeTask>()
                        .eq(AudioEncodeTask::getVideoId, videoId)
                        .eq(AudioEncodeTask::getAudioQualityCode, qualityCode)
        );

        audioEncodeTask.setCompleteAt(LocalDateTime.now());
        audioEncodeTask.setResultAudioUrl(resultAudioUrl);
        audioEncodeTaskMapper.update(
                audioEncodeTask,
                new LambdaQueryWrapper<AudioEncodeTask>()
                        .eq(AudioEncodeTask::getVideoId, videoId)
                        .eq(AudioEncodeTask::getAudioQualityCode, qualityCode)
        );
    }

    @Override
    public List<VideoEncodeTask> getVideoEncodeTasks(Long videoId) {
        return videoEncodeTaskMapper.selectList(new LambdaQueryWrapper<VideoEncodeTask>().eq(VideoEncodeTask::getVideoId, videoId));
    }

    @Override
    public List<AudioEncodeTask> getAudioEncodeTasks(Long videoId) {
        return audioEncodeTaskMapper.selectList(new LambdaQueryWrapper<AudioEncodeTask>().eq(AudioEncodeTask::getVideoId, videoId));
    }
}
