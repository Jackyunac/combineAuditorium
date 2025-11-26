package com.combine.auditorium.service;

import com.combine.auditorium.entity.Video;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VideoService {
    Video uploadVideo(MultipartFile file, MultipartFile cover, String title, String description, String category, String rating, String videoType, String region, Integer releaseYear, Long userId);
    List<Video> listVideos(String keyword, String category, String videoType, String region, Integer year, Boolean isHot, String sort);
    Video getVideoById(Long id);
    void deleteVideo(Long id, Long userId, String role);
    void updateVideo(Video video);
    void incrementViewCount(Long id);
    List<Video> listAllVideosForAdmin(); // For management page
}

