package com.combine.auditorium.service;

import com.combine.auditorium.entity.ForumComment;
import com.combine.auditorium.entity.ForumPost;

import java.util.List;

public interface ForumService {

    ForumPost createPost(String title, String content, Long userId);

    List<ForumPost> listPosts(int limit);

    ForumPost getPost(Long id);

    void deletePost(Long id, Long userId, String role);

    ForumComment addComment(Long postId, String content, Long userId);

    List<ForumComment> listComments(Long postId);

    void deleteComment(Long id, Long userId, String role);
}
