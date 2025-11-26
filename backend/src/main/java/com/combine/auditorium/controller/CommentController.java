package com.combine.auditorium.controller;

import com.combine.auditorium.common.Result;
import com.combine.auditorium.entity.Comment;
import com.combine.auditorium.entity.User;
import com.combine.auditorium.service.CommentService;
import com.combine.auditorium.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final UserService userService;

    @PostMapping
    public Result<String> addComment(@RequestBody Map<String, Object> payload, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        
        Long videoId = Long.valueOf(payload.get("videoId").toString());
        String content = (String) payload.get("content");
        
        if (content == null || content.trim().isEmpty()) {
            return Result.error("Content cannot be empty");
        }
        
        commentService.addComment(videoId, content, userId);
        return Result.success("Comment added");
    }

    @GetMapping
    public Result<List<Comment>> listComments(@RequestParam Long videoId) {
        return Result.success(commentService.listComments(videoId));
    }

    @DeleteMapping("/{id}")
    public Result<String> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) return Result.error(401, "Unauthorized");
        
        User user = userService.getById(userId);
        if (user == null) return Result.error(401, "Unauthorized");
        try {
            commentService.deleteComment(id, userId, user.getRole());
            return Result.success("Deleted");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}

