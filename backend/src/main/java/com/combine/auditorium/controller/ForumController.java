package com.combine.auditorium.controller;

import com.combine.auditorium.common.Result;
import com.combine.auditorium.common.RoleConstants;
import com.combine.auditorium.entity.ForumComment;
import com.combine.auditorium.entity.ForumPost;
import com.combine.auditorium.entity.User;
import com.combine.auditorium.service.ForumService;
import com.combine.auditorium.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
@RequiredArgsConstructor
public class ForumController {

    private final ForumService forumService;
    private final UserService userService;

    @PostMapping("/posts")
    public Result<ForumPost> createPost(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        String title = payload.get("title");
        String content = payload.get("content");
        return Result.success(forumService.createPost(title, content, userId));
    }

    @GetMapping("/posts")
    public Result<List<ForumPost>> listPosts(@RequestParam(value = "limit", defaultValue = "50") Integer limit) {
        return Result.success(forumService.listPosts(limit));
    }

    @GetMapping("/posts/{id}")
    public Result<ForumPost> getPost(@PathVariable Long id) {
        return Result.success(forumService.getPost(id));
    }

    @DeleteMapping("/posts/{id}")
    public Result<String> deletePost(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        User user = userService.getById(userId);
        if (user == null) return Result.error(401, "Unauthorized");
        forumService.deletePost(id, userId, user.getRole());
        return Result.success("Deleted");
    }

    @PostMapping("/posts/{postId}/comments")
    public Result<ForumComment> addComment(@PathVariable Long postId,
                                           @RequestBody Map<String, String> payload,
                                           HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        String content = payload.get("content");
        return Result.success(forumService.addComment(postId, content, userId));
    }

    @GetMapping("/posts/{postId}/comments")
    public Result<List<ForumComment>> listComments(@PathVariable Long postId) {
        return Result.success(forumService.listComments(postId));
    }

    @DeleteMapping("/comments/{id}")
    public Result<String> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        User user = userService.getById(userId);
        if (user == null) return Result.error(401, "Unauthorized");
        forumService.deleteComment(id, userId, user.getRole());
        return Result.success("Deleted");
    }
}
