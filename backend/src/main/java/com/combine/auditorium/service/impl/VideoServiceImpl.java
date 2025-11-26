package com.combine.auditorium.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.combine.auditorium.common.RoleConstants;
import com.combine.auditorium.entity.Video;
import com.combine.auditorium.mapper.VideoMapper;
import com.combine.auditorium.service.AsyncTranscodeService;
import com.combine.auditorium.service.MinioService;
import com.combine.auditorium.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;

/**
 * Video business service implementation.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class VideoServiceImpl implements VideoService {

    private static final long MAX_VIDEO_SIZE_BYTES = 1024L * 1024 * 1024; // 1GB soft limit

    private final VideoMapper videoMapper;
    private final MinioService minioService;
    private final AsyncTranscodeService asyncTranscodeService;

    /**
     * Upload video then trigger async transcode.
     */
    @Override
    public Video uploadVideo(MultipartFile file, MultipartFile cover, String title, String description, String category, String rating, String videoType, String region, Integer releaseYear, Long userId) {
        validateUpload(file, cover);
        String objectName = minioService.upload(file);

        Video video = new Video();
        video.setUserId(userId);
        video.setTitle(title);
        video.setDescription(description);
        video.setCategory(category);
        video.setRating(rating);
        video.setVideoType(videoType);
        video.setRegion(region);
        video.setReleaseYear(releaseYear);
        video.setFileId(objectName);
        video.setStatus(0);
        video.setViewCount(0L);
        video.setCommentCount(0L);

        if (cover != null && !cover.isEmpty()) {
            String coverObjectName = minioService.upload(cover);
            video.setCoverUrl(minioService.getPublicUrl(coverObjectName));
        }

        videoMapper.insert(video);
        asyncTranscodeService.transcode(video.getId(), objectName);
        return video;
    }

    @Override
    public List<Video> listVideos(String keyword, String category, String videoType, String region, Integer year, Boolean isHot, String sort) {
        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Video::getStatus, 1);

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Video::getTitle, keyword);
        }
        if (category != null && !category.isEmpty() && !"All".equalsIgnoreCase(category)) {
            wrapper.eq(Video::getCategory, category);
        }
        if (videoType != null && !videoType.isEmpty() && !"All".equalsIgnoreCase(videoType)) {
            wrapper.eq(Video::getVideoType, videoType);
        }
        if (region != null && !region.isEmpty() && !"All".equalsIgnoreCase(region)) {
            wrapper.eq(Video::getRegion, region);
        }
        if (year != null) {
            wrapper.eq(Video::getReleaseYear, year);
        }
        if (isHot != null && isHot) {
            wrapper.eq(Video::getIsHot, true);
        }

        if ("newest".equalsIgnoreCase(sort)) {
            wrapper.orderByDesc(Video::getCreateTime);
        } else if ("hottest".equalsIgnoreCase(sort)) {
            wrapper.orderByDesc(Video::getViewCount);
        } else if ("upcoming".equalsIgnoreCase(sort)) {
            wrapper.orderByDesc(Video::getReleaseYear);
        } else {
            wrapper.orderByDesc(Video::getCreateTime);
        }

        return videoMapper.selectList(wrapper);
    }

    @Override
    public Video getVideoById(Long id) {
        return videoMapper.selectById(id);
    }

    @Override
    public void deleteVideo(Long id, Long userId, String role) {
        Video video = videoMapper.selectById(id);
        if (video == null) {
            throw new RuntimeException("Video not found");
        }
        if (!video.getUserId().equals(userId) && !RoleConstants.SYSTEM.equals(role)) {
            throw new RuntimeException("Permission denied");
        }

        videoMapper.deleteById(id);
        minioService.deleteVideoObjects(video);
    }

    @Override
    public void updateVideo(Video video) {
        videoMapper.updateById(video);
    }

    @Override
    public void incrementViewCount(Long id) {
        LambdaUpdateWrapper<Video> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Video::getId, id)
                .setSql("view_count = IFNULL(view_count, 0) + 1");
        int updated = videoMapper.update(null, updateWrapper);
        if (updated == 0) {
            log.warn("View count increment skipped, video not found: {}", id);
        }
    }

    @Override
    public List<Video> listAllVideosForAdmin() {
        return videoMapper.selectList(null); // Return all videos including off-shelf
    }

    private void validateUpload(MultipartFile file, MultipartFile cover) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Video file is required");
        }
        if (file.getSize() > MAX_VIDEO_SIZE_BYTES) {
            throw new RuntimeException("Video file is too large");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("video/")) {
            throw new RuntimeException("Invalid video file type");
        }
        if (cover != null && !cover.isEmpty()) {
            String coverType = cover.getContentType();
            if (coverType == null || !coverType.toLowerCase(Locale.ROOT).startsWith("image/")) {
                throw new RuntimeException("Cover must be an image");
            }
        }
    }
}
