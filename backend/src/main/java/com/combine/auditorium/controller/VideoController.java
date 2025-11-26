package com.combine.auditorium.controller;

import com.combine.auditorium.common.Result;
import com.combine.auditorium.common.RoleConstants;
import com.combine.auditorium.entity.User;
import com.combine.auditorium.entity.Video;
import com.combine.auditorium.service.UserService;
import com.combine.auditorium.service.VideoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 视频控制器
 * 处理视频上传、列表查询、详情获取
 */
@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoService videoService;
    private final UserService userService;

    /**
     * 上传视频接口
     * Content-Type: multipart/form-data
     * 
     * @param file 视频文件
     * @param title 标题
     * @param description 描述 (可选)
     * @param request HTTP 请求对象 (用于获取拦截器存入的 userId)
     * @return 创建的视频记录
     */
    @PostMapping("/upload")
    public Result<Video> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "cover", required = false) MultipartFile cover,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "category", defaultValue = "Uncategorized") String category,
            @RequestParam(value = "rating", defaultValue = "G") String rating,
            @RequestParam(value = "videoType", required = false) String videoType,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "releaseYear", required = false) Integer releaseYear,
            HttpServletRequest request) {
        
        // 从 Request 属性中获取当前登录用户ID (由 LoginInterceptor 设置)
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "Unauthorized");
        }
        
        // 权限校验：仅 SYSTEM 角色可上传
        User user = userService.getById(userId);
        if (user == null || !RoleConstants.SYSTEM.equals(user.getRole())) {
            return Result.error(403, "Permission denied: Only SYSTEM users can upload videos");
        }
        
        try {
            Video video = videoService.uploadVideo(file, cover, title, description, category, rating, videoType, region, releaseYear, userId);
            return Result.success(video);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取视频列表接口
     * 支持搜索和分类筛选
     */
    @GetMapping
    public Result<List<Video>> list(
            @RequestParam(value = "q", required = false) String keyword,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "videoType", required = false) String videoType,
            @RequestParam(value = "region", required = false) String region,
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "isHot", required = false) Boolean isHot,
            @RequestParam(value = "sort", defaultValue = "newest") String sort) {
        return Result.success(videoService.listVideos(keyword, category, videoType, region, year, isHot, sort));
    }

    /**
     * 获取视频详情接口
     * @param id 视频ID
     * @return 视频详情信息
     */
    @GetMapping("/{id}")
    public Result<Video> getDetail(@PathVariable Long id) {
        // 增加播放量
        videoService.incrementViewCount(id);
        return Result.success(videoService.getVideoById(id));
    }

    /**
     * 管理员获取所有视频列表
     */
    @GetMapping("/admin/list")
    public Result<List<Video>> adminList(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        
        User user = userService.getById(userId);
        if (user == null || !RoleConstants.SYSTEM.equals(user.getRole())) {
            return Result.error(403, "Permission denied");
        }
        
        return Result.success(videoService.listAllVideosForAdmin());
    }

    /**
     * 管理员更新视频信息 (上架/下架, 上热门, 修改信息)
     */
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Video video, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        
        User user = userService.getById(userId);
        if (user == null || !RoleConstants.SYSTEM.equals(user.getRole())) {
            return Result.error(403, "Permission denied");
        }
        
        Video existing = videoService.getVideoById(id);
        if (existing == null) return Result.error("Video not found");
        
        // 更新字段
        if (video.getTitle() != null) existing.setTitle(video.getTitle());
        if (video.getDescription() != null) existing.setDescription(video.getDescription());
        if (video.getStatus() != null) existing.setStatus(video.getStatus());
        if (video.getIsHot() != null) existing.setIsHot(video.getIsHot());
        if (video.getCategory() != null) existing.setCategory(video.getCategory());
        
        try {
            videoService.updateVideo(existing);
            return Result.success("Updated successfully");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除视频接口
     */
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        
        User user = userService.getById(userId);
        if (user == null) return Result.error(401, "Unauthorized");
        try {
            videoService.deleteVideo(id, userId, user.getRole());
            return Result.success("Deleted successfully");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
