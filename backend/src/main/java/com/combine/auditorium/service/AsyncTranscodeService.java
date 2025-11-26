package com.combine.auditorium.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.combine.auditorium.entity.Video;
import com.combine.auditorium.mapper.VideoMapper;
import com.combine.auditorium.utils.FFmpegUtils;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Async transcode service for video uploads.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncTranscodeService {

    private final MinioClient minioClient;
    private final MinioService minioService;
    private final FFmpegUtils ffmpegUtils;
    private final VideoMapper videoMapper;

    @Value("${minio.bucket-name}")
    private String bucketName;

    @Async
    public void transcode(Long videoId, String objectName) {
        log.info("Start async transcoding for video: {}", videoId);
        String tempDir = System.getProperty("java.io.tmpdir") + File.separator + "transcode_" + IdUtil.simpleUUID();
        FileUtil.mkdir(tempDir);

        try {
            Video currentVideo = videoMapper.selectById(videoId);
            if (currentVideo == null) {
                log.warn("Transcoding aborted: video {} not found", videoId);
                return;
            }
            boolean hasCover = currentVideo.getCoverUrl() != null && !currentVideo.getCoverUrl().isEmpty();

            String originalFilePath = tempDir + File.separator + "input.mp4";
            log.info("Downloading file from MinIO: {}", objectName);

            try (InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build())) {
                Files.copy(stream, new File(originalFilePath).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            String coverUrl = null;
            if (!hasCover) {
                String coverPath = ffmpegUtils.generateCover(originalFilePath, tempDir, "cover");
                if (coverPath != null) {
                    String coverObjectName = "covers/" + videoId + ".jpg";
                    minioService.uploadLocalFile(coverPath, coverObjectName, "image/jpeg");
                    coverUrl = minioService.getPublicUrl(coverObjectName);
                }
            }

            ffmpegUtils.transcodeToHls(originalFilePath, tempDir, "index");

            File[] files = new File(tempDir).listFiles();
            String m3u8ObjectName = null;
            if (files != null) {
                for (File f : files) {
                    if (f.getName().endsWith(".m3u8") || f.getName().endsWith(".ts")) {
                        String objName = "videos/" + videoId + "/" + f.getName();
                        String contentType = f.getName().endsWith(".m3u8") ? "application/x-mpegURL" : "video/MP2T";
                        minioService.uploadLocalFile(f.getAbsolutePath(), objName, contentType);
                        if (f.getName().endsWith(".m3u8")) {
                            m3u8ObjectName = objName;
                        }
                    }
                }
            }

            Video video = videoMapper.selectById(videoId);
            if (video == null) {
                log.warn("Transcoding completed but video {} missing during update", videoId);
                return;
            }
            video.setStatus(1);
            if (coverUrl != null) {
                video.setCoverUrl(coverUrl);
            }
            if (m3u8ObjectName != null) {
                video.setVideoUrl(minioService.getPublicUrl(m3u8ObjectName));
            }
            videoMapper.updateById(video);

            log.info("Transcoding completed for video: {}", videoId);
        } catch (Exception e) {
            log.error("Transcoding failed", e);
            Video video = videoMapper.selectById(videoId);
            if (video != null) {
                video.setStatus(2);
                videoMapper.updateById(video);
            } else {
                log.warn("Transcoding failed and video {} missing, status not updated", videoId);
            }
        } finally {
            FileUtil.del(tempDir);
        }
    }
}
