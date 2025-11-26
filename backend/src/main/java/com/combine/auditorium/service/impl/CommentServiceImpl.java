package com.combine.auditorium.service.impl;

import com.combine.auditorium.common.RoleConstants;
import com.combine.auditorium.entity.Comment;
import com.combine.auditorium.entity.Video;
import com.combine.auditorium.mapper.CommentMapper;
import com.combine.auditorium.mapper.VideoMapper;
import com.combine.auditorium.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final VideoMapper videoMapper;

    @Override
    @Transactional
    public void addComment(Long videoId, String content, Long userId) {
        Comment comment = new Comment();
        comment.setVideoId(videoId);
        comment.setUserId(userId);
        comment.setContent(content);
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);

        // Update video comment count
        Video video = videoMapper.selectById(videoId);
        if (video != null) {
            long currentCount = video.getCommentCount() == null ? 0L : video.getCommentCount();
            video.setCommentCount(currentCount + 1);
            videoMapper.updateById(video);
        }
    }

    @Override
    public List<Comment> listComments(Long videoId) {
        return commentMapper.selectCommentsByVideoId(videoId);
    }

    @Override
    @Transactional
    public void deleteComment(Long id, Long userId, String role) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) {
            return;
        }

        // Only the owner or system role can delete
        if (!comment.getUserId().equals(userId) && !RoleConstants.SYSTEM.equals(role)) {
            throw new RuntimeException("Permission denied");
        }

        commentMapper.deleteById(id);

        // Update video comment count
        Video video = videoMapper.selectById(comment.getVideoId());
        if (video != null) {
            long currentCount = video.getCommentCount() == null ? 0L : video.getCommentCount();
            video.setCommentCount(Math.max(0, currentCount - 1));
            videoMapper.updateById(video);
        }
    }
}
