package com.inkneko.nekowindow.encode.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkneko.nekowindow.common.ServiceException;
import com.inkneko.nekowindow.common.util.OssUtils;
import com.inkneko.nekowindow.encode.entity.ProbeResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class MediaUtils {

    /**
     * 对指定文件进行分析，获取媒体文件中的视频流和音频流信息，参见{@link com.inkneko.nekowindow.encode.entity.ProbeResult}
     *
     * @param mediaFile 媒体文件
     * @return 视频流和音频流信息
     * @throws IOException          IO异常
     * @throws InterruptedException 中断异常
     */
    public static ProbeResult probeVideo(File mediaFile) throws IOException, InterruptedException {
        ObjectMapper objectMapper = new ObjectMapper();

        //调用ffprobe分析视频格式
        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffprobe",
                "-v", "quiet",
                "-of", "json",
                "-show_format",
                "-show_streams",
                mediaFile.getAbsolutePath()
        );

        Process process = processBuilder.start();
        StringBuilder stdoutStringBuilder = new StringBuilder();
        StringBuilder stderrStringBuilder = new StringBuilder();
        Thread stdoutReadingThead = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = process.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String tmp;
                try {
                    while ((tmp = bufferedReader.readLine()) != null) {
                        stdoutStringBuilder.append(tmp);
                    }
                    inputStream.close();
                } catch (IOException e) {
                    log.error("读取ffprobe输出时发生异常", e);
                }
            }
        });
        Thread stderrReadingThead = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = process.getErrorStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String tmp;
                try {
                    while ((tmp = bufferedReader.readLine()) != null) {
                        stderrStringBuilder.append(tmp);
                    }
                    inputStream.close();
                } catch (IOException e) {
                    log.error("读取ffprobe stderr输出时发生异常", e);
                }
            }
        });
        stdoutReadingThead.start();
        stderrReadingThead.start();

        int retCode = process.waitFor();
        stdoutReadingThead.join();
        stderrReadingThead.join();

        mediaFile.deleteOnExit();

        if (retCode != 0) {
            throw new IOException(String.format("ffprobe执行失败，命令：%s，stderr输出：%s", processBuilder, stderrStringBuilder));
        }

        return objectMapper.readValue(stdoutStringBuilder.toString(), ProbeResult.class);
    }

    /**
     * @param mediaFileLink   视频文件链接/文件绝对路径
     * @param outputFile      输出文件
     * @param segmentStartPos 分片起始位置
     * @param segmentSize     分片大小
     * @param codec           编码格式
     * @param maxBitrate      最大码率
     * @param frameRate       帧率
     * @param intervalSize    关键帧间隔，以秒为单位
     * @param scale           缩放参数
     * @throws IOException          文件读写错误时抛出该异常
     * @throws InterruptedException 被中断时抛出该异常
     * @throws ServiceException     文件处理错误时抛出该异常
     */
    public static void encodeVideo(String mediaFileLink,
                                   File outputFile,
                                   String segmentStartPos,
                                   String segmentSize,
                                   String codec,
                                   String maxBitrate,
                                   String frameRate,
                                   Integer intervalSize,
                                   String scale)
            throws IOException, InterruptedException, ServiceException {

        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-v", "error",
                "-ss", segmentStartPos,
                "-i", mediaFileLink,
                "-t", segmentSize,
                "-an",
                "-segment_time", segmentSize,
                "-reset_timestamps", "1",
                "-c:v", codec,
                "-b:v", maxBitrate,
                "-maxrate", maxBitrate,
                "-bufsize", maxBitrate,
                "-crf", "20",
                "-r", frameRate,
                "-keyint_min", String.format("%s*%d", frameRate, intervalSize),
                "-g", String.format("%s*%d", frameRate, intervalSize),
                //"-force_key_frames", String.format("expr:gte(t, floor(t/%d + 1e-3)*%d)", intervalSize, intervalSize), //AI给了个对其关键帧的解决办法，没看明白，最后搞得20ms一个关键帧
                "-sc_threshold", "0",
                "-filter:v", "scale=" + scale,
                outputFile.getAbsolutePath()
        );
        log.info("命令：{}", processBuilder.command().toString());
        executeTaskAndGetResult(processBuilder);
    }

    /**
     * @param mediaFileLink 视频文件链接/文件绝对路径
     * @param outputFile    输出文件
     * @param codec         编码格式
     * @param maxBitrate    最大码率
     * @throws IOException          文件读写错误时抛出该异常
     * @throws InterruptedException 被中断时抛出该异常
     * @throws ServiceException     文件处理错误时抛出该异常
     */
    public static void encodeAudio(String mediaFileLink,
                                   File outputFile,
                                   String codec,
                                   String maxBitrate)
            throws IOException, InterruptedException, ServiceException {


        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-v", "error",
                "-i", mediaFileLink,
                "-vn",
                "-c:a", codec,
                "-b:a", maxBitrate,
                "-maxrate", maxBitrate,
                "-bufsize", maxBitrate,
                outputFile.getAbsolutePath()
        );
        executeTaskAndGetResult(processBuilder);

    }

    /**
     * 调用ffmpeg -c concat进行合并
     * <p>
     * inputVideosTxtFile 的文件内容示例如下：
     * <p>
     * file 1-1740315274203-h264-8M-1-11-1106701775879466896.mp4
     * <p>
     * file 1-1740315274279-h264-8M-2-11-2987054616855148116.mp4
     * <p>
     * file 1-1740315274350-h264-8M-3-11-8502616567310792746.mp4
     * <p>
     * file 1-1740315376106-h264-8M-4-11-3879596219307263676.mp4
     * <p>
     * ...
     * <p>
     * file 1-1740315535468-h264-8M-12-11-599332897789739949.mp4
     *
     * @param inputVideosTxtFile 输入文件，文件顺序将以列表顺序排列
     * @param outputFile         输出文件
     * @throws IOException          文件读写错误时抛出该异常
     * @throws InterruptedException 被中断时抛出该异常
     */
    public static void concatVideo(File inputVideosTxtFile, File outputFile) throws IOException, InterruptedException {
        /*
        ffmpeg -f concat -i 1080phigh.txt -c copy -sc_threshold 0 -map 0 output-1080phigh.mp4
         */

        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffmpeg",
                "-y",
                "-v", "error",
                "-f", "concat",
                "-i", inputVideosTxtFile.getAbsolutePath(),
                "-c", "copy",
                "-sc_threshold", "0",
                "-map", "0",
                outputFile.getAbsolutePath()
        );
        executeTaskAndGetResult(processBuilder);
    }

    public static void dashGenerate(List<String> inputVideos, List<String> inputAudios, File outputFile, String singleFileTemplate) throws IOException, InterruptedException {
        /*
          ffmpeg
            -i output-1080phigh.mp4 -i output-1080p.mp4 -i output-720p.mp4 -i output-360p.mp4 -i output-128k.m4a
            -c copy
            -map 0:v:0 -map 1:v:0 -map 2:v:0 -map 3:v:0 -map 4:a:0
            -single_file 1
            -adaptation_sets  "id=0,streams=v id=1,streams=a"
            -dash_segment_type mp4
            -f dash source.mpd
         */

        List<String> commands = new ArrayList<>();
        commands.add("ffmpeg");
        commands.add("-y");
        commands.add("-v");
        commands.add("error");
        for (String video : inputVideos) {
            commands.add("-i");
            commands.add(video);
        }
        for (String video : inputAudios) {
            commands.add("-i");
            commands.add(video);
        }
        commands.add("-c");
        commands.add("copy");
        int counter = 0;
        for (int i = 0; i < inputVideos.size(); i++) {
            commands.add("-map");
            commands.add("%d:v:0".formatted(counter++));
        }
        for (String audio : inputAudios) {
            commands.add("-map");
            commands.add("%d:a:0".formatted(counter++));
        }
        commands.add("-single_file");
        commands.add("1");
        commands.add("-adaptation_sets");
        commands.add("id=0,streams=v id=1,streams=a");
        commands.add("-dash_segment_type");
        commands.add("mp4");
        commands.add("-single_file_name");
        commands.add(singleFileTemplate);
        commands.add("-f");
        commands.add("dash");
        commands.add(outputFile.getAbsolutePath());


        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        executeTaskAndGetResult(processBuilder);
    }

    private static void executeTaskAndGetResult(ProcessBuilder processBuilder) throws IOException, InterruptedException, ServiceException {
        Process process = processBuilder.start();
        log.info("执行命令: {}", process.info().commandLine().toString());
        StringBuilder stdoutStringBuilder = new StringBuilder();
        StringBuilder stderrStringBuilder = new StringBuilder();
        Thread stdoutReadingThead = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = process.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String tmp;
                try {
                    while ((tmp = bufferedReader.readLine()) != null) {
                        stdoutStringBuilder.append(tmp);
                    }
                } catch (IOException e) {
                    log.error("读取stdout发生错误：", e);
                }

            }
        });
        Thread stderrReadingThead = new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = process.getErrorStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String tmp;
                try {

                    while ((tmp = bufferedReader.readLine()) != null) {
                        stderrStringBuilder.append(tmp);
                    }
                } catch (IOException e) {
                    log.error("读取stderr发生错误：", e);
                }
            }
        });

        stdoutReadingThead.start();
        stderrReadingThead.start();
        int retCode = process.waitFor();
        stdoutReadingThead.join();
        stderrReadingThead.join();
        if (retCode == 128 + 9 || retCode == 128 + 15 || retCode == 255) {
            throw new InterruptedException("进程被SIGTERM或SIGKILL，返回值：" + retCode);
        }

        if (retCode != 0) {
            log.error("执行视频转码时ffmpeg返回值 {} 不为0, stdout: {}, stderr: {}", retCode, stdoutStringBuilder, stderrStringBuilder);
            throw new ServiceException(500, "ffmpeg执行时发生错误");
        }
    }
}
