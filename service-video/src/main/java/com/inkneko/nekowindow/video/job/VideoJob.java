package com.inkneko.nekowindow.video.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.inkneko.nekowindow.video.entity.VideoPost;
import com.inkneko.nekowindow.video.entity.VideoPostResource;
import com.inkneko.nekowindow.video.mapper.VideoPostMapper;
import com.inkneko.nekowindow.video.mapper.VideoPostResourceMapper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class VideoJob {

    private final RedissonClient redissonClient;
    private final VideoPostMapper videoPostMapper;
    private final VideoPostResourceMapper videoPostResourceMapper;

    public VideoJob(RedissonClient redissonClient, VideoPostMapper videoPostMapper, VideoPostResourceMapper videoPostResourceMapper) {
        this.redissonClient = redissonClient;
        this.videoPostMapper = videoPostMapper;
        this.videoPostResourceMapper = videoPostResourceMapper;
    }
    @Scheduled(fixedDelay = 5000)
    public void flushViewCount(){
        RLock lock = redissonClient.getLock("job:video:view:flush");
        boolean locked = false;
        try {
            locked = lock.tryLock(0, 30, TimeUnit.SECONDS);
            if (!locked) {
                return;
            }

            RSet<Long> dirtyVideos = redissonClient.getSet("video:view:dirty");

            for (Long videoId : dirtyVideos.readAll()) {
                long count = redissonClient
                        .getAtomicLong("video:view_count:" + videoId)
                        .getAndSet(0);

                if (count > 0) {
                    VideoPostResource videoPostResource = videoPostResourceMapper.selectById(videoId);
                    if (videoPostResource != null){
                        videoPostResourceMapper.update(
                                null,
                                Wrappers.<VideoPostResource>lambdaUpdate()
                                        .eq(VideoPostResource::getVideoId, videoId)
                                        .setSql("visit = visit + " + count)
                        );
                        videoPostMapper.update(
                                null,
                                Wrappers.<VideoPost>lambdaUpdate()
                                        .eq(VideoPost::getNkid, videoPostResource.getNkid())
                                        .setSql("visit = visit + " + count)
                        );
                    }

                }

                dirtyVideos.remove(videoId);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("tryLock interrupted, exit task", e);
        }finally {
            if (locked){
                lock.unlock();
            }
        }
    }
}
