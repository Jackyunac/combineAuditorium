package com.combine.auditorium.service;

import com.combine.auditorium.entity.Comment;
import java.util.List;

public interface CommentService {
    void addComment(Long videoId, String content, Long userId);
    List<Comment> listComments(Long videoId);
    void deleteComment(Long id, Long userId, String role);
}

