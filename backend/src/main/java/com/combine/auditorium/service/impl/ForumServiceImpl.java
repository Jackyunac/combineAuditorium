package com.combine.auditorium.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.combine.auditorium.common.RoleConstants;
import com.combine.auditorium.entity.ForumComment;
import com.combine.auditorium.entity.ForumPost;
import com.combine.auditorium.entity.User;
import com.combine.auditorium.mapper.ForumCommentMapper;
import com.combine.auditorium.mapper.ForumPostMapper;
import com.combine.auditorium.service.ForumService;
import com.combine.auditorium.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ForumServiceImpl implements ForumService {

    private static final int MAX_POST_LIMIT = 200;

    private final ForumPostMapper forumPostMapper;
    private final ForumCommentMapper forumCommentMapper;
    private final UserService userService;

    @Override
    public ForumPost createPost(String title, String content, Long userId) {
        if (title == null || title.trim().isEmpty()) {
            throw new RuntimeException("标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("内容不能为空");
        }
        ForumPost post = new ForumPost();
        post.setUserId(userId);
        post.setTitle(title.trim());
        post.setContent(content.trim());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        forumPostMapper.insert(post);
        attachUser(post);
        return post;
    }

    @Override
    public List<ForumPost> listPosts(int limit) {
        int rows = limit <= 0 ? 50 : Math.min(limit, MAX_POST_LIMIT);
        LambdaQueryWrapper<ForumPost> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(ForumPost::getUpdatedAt).last("limit " + rows);
        List<ForumPost> list = forumPostMapper.selectList(wrapper);
        list.forEach(this::attachUser);
        return list;
    }

    @Override
    public ForumPost getPost(Long id) {
        ForumPost post = forumPostMapper.selectById(id);
        attachUser(post);
        return post;
    }

    @Override
    public void deletePost(Long id, Long userId, String role) {
        ForumPost post = forumPostMapper.selectById(id);
        if (post == null) return;
        if (!post.getUserId().equals(userId) && !RoleConstants.SYSTEM.equals(role)) {
            throw new RuntimeException("无权限删除帖子");
        }
        forumPostMapper.deleteById(id);
        forumCommentMapper.delete(new LambdaQueryWrapper<ForumComment>().eq(ForumComment::getPostId, id));
    }

    @Override
    @Transactional
    public ForumComment addComment(Long postId, String content, Long userId) {
        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("评论不能为空");
        }
        ForumComment comment = new ForumComment();
        comment.setPostId(postId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        comment.setCreatedAt(LocalDateTime.now());
        forumCommentMapper.insert(comment);
        attachUser(comment);

        // 更新帖子更新时间，便于按活跃排序
        ForumPost post = forumPostMapper.selectById(postId);
        if (post != null) {
            post.setUpdatedAt(LocalDateTime.now());
            forumPostMapper.updateById(post);
        }
        return comment;
    }

    @Override
    public List<ForumComment> listComments(Long postId) {
        LambdaQueryWrapper<ForumComment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ForumComment::getPostId, postId).orderByAsc(ForumComment::getCreatedAt);
        List<ForumComment> list = forumCommentMapper.selectList(wrapper);
        list.forEach(this::attachUser);
        return list;
    }

    @Override
    public void deleteComment(Long id, Long userId, String role) {
        ForumComment comment = forumCommentMapper.selectById(id);
        if (comment == null) return;
        if (!comment.getUserId().equals(userId) && !RoleConstants.SYSTEM.equals(role)) {
            throw new RuntimeException("无权限删除评论");
        }
        forumCommentMapper.deleteById(id);
    }

    private void attachUser(ForumPost post) {
        if (post == null) return;
        User user = userService.getById(post.getUserId());
        if (user != null) {
            post.setNickname(user.getNickname());
            post.setAvatar(user.getAvatar());
        }
    }

    private void attachUser(ForumComment comment) {
        if (comment == null) return;
        User user = userService.getById(comment.getUserId());
        if (user != null) {
            comment.setNickname(user.getNickname());
            comment.setAvatar(user.getAvatar());
        }
    }
}
