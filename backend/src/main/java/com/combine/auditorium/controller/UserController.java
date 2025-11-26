package com.combine.auditorium.controller;

import com.combine.auditorium.common.Result;
import com.combine.auditorium.entity.User;
import com.combine.auditorium.service.MinioService;
import com.combine.auditorium.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MinioService minioService;

    @GetMapping("/info")
    public Result<User> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "Unauthorized");
        }
        
        User user = userService.getById(userId);
        if (user != null) {
            user.setPassword(null); // 隐藏密码
            // 如果数据库中没有头像，给予默认头像返回，但不修改数据库
            if (user.getAvatar() == null || user.getAvatar().isEmpty()) {
                user.setAvatar("https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png");
            }
        }
        return Result.success(user);
    }

    @PostMapping("/avatar")
    public Result<String> updateAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.error(401, "Unauthorized");
        }

        try {
            // 1. 上传到 MinIO
            String objectName = minioService.upload(file);
            // 2. 获取公开访问链接
            // 注意：这里假设 Bucket 是 Public 的
            String avatarUrl = minioService.getPublicUrl(objectName);
            
            // 3. 更新数据库
            userService.updateAvatar(userId, avatarUrl);
            
            return Result.success(avatarUrl);
        } catch (Exception e) {
            return Result.error("Upload failed: " + e.getMessage());
        }
    }
}
