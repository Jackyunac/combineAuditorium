package com.combine.auditorium.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * FFmpeg 工具类
 * 负责调用系统安装的 ffmpeg 命令进行视频处理
 */
@Slf4j
@Component
public class FFmpegUtils {

    @Value("${ffmpeg.path:ffmpeg}")
    private String ffmpegPath;

    /**
     * 转码为 HLS (m3u8)
     * HLS 协议适合流媒体播放，会将视频切片为多个 .ts 文件
     * 
     * @param inputPath 本地输入文件路径 (如 /tmp/input.mp4)
     * @param outputDir 输出目录 (如 /tmp/output/)
     * @param filePrefix 输出文件前缀 (如 video_123)
     * @return 生成的 m3u8 文件绝对路径
     */
    public String transcodeToHls(String inputPath, String outputDir, String filePrefix) {
        // 确保输出目录存在
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String m3u8Path = outputDir + File.separator + filePrefix + ".m3u8";

        // 构建 FFmpeg 命令
        // 命令示例: ffmpeg -i input.mp4 -c:v libx264 -c:a aac -hls_time 10 -hls_list_size 0 -f hls output.m3u8
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-i");
        command.add(inputPath);
        
        // 视频编码: H.264
        command.add("-c:v");
        command.add("libx264"); 
        
        // 转码预设: veryfast (牺牲少量压缩率换取速度)
        command.add("-preset");
        command.add("veryfast"); 
        
        // 音频编码: AAC
        command.add("-c:a");
        command.add("aac");     
        
        // HLS 切片时间: 10秒一个切片
        command.add("-hls_time");
        command.add("10");      
        
        // HLS 列表大小: 0 表示保留所有切片 (点播模式), 不设置为直播模式
        command.add("-hls_list_size");
        command.add("0");       
        
        // 输出格式
        command.add("-f");
        command.add("hls");
        command.add(m3u8Path);

        log.info("Start transcoding: {}", String.join(" ", command));
        
        try {
            // 使用 Hutool 执行命令
            Process process = RuntimeUtil.exec(command.toArray(new String[0]));
            // 必须读取输出流，否则可能会导致进程阻塞
            String result = RuntimeUtil.getResult(process);
            log.info("Transcoding finished. Output: {}", result);
            
            // 检查文件是否生成成功
            if (!FileUtil.exist(m3u8Path)) {
                throw new RuntimeException("Transcoding failed: Output file not found");
            }
            
            return m3u8Path;
        } catch (Exception e) {
            log.error("Transcoding error", e);
            throw new RuntimeException("Transcoding failed");
        }
    }

    /**
     * 获取视频截图 (用作封面)
     * 
     * @param inputPath 本地视频文件路径
     * @param outputDir 输出目录
     * @param filePrefix 输出文件名前缀
     * @return 生成的封面图片路径
     */
    public String generateCover(String inputPath, String outputDir, String filePrefix) {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        String coverPath = outputDir + File.separator + filePrefix + ".jpg";
        
        // 命令示例: ffmpeg -i input.mp4 -ss 00:00:01 -vframes 1 output.jpg
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-i");
        command.add(inputPath);
        // 截取第 1 秒的帧
        command.add("-ss");
        command.add("00:00:01");
        // 只输出 1 帧
        command.add("-vframes");
        command.add("1");
        command.add(coverPath);
        
        try {
            Process process = RuntimeUtil.exec(command.toArray(new String[0]));
            RuntimeUtil.getResult(process);
            return coverPath;
        } catch (Exception e) {
            log.error("Generate cover error", e);
            return null;
        }
    }
}
