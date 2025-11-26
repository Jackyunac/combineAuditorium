package com.combine.auditorium.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.combine.auditorium.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    
    @Select("SELECT c.*, u.nickname, u.avatar FROM video_comment c " +
            "LEFT JOIN sys_user u ON c.user_id = u.id " +
            "WHERE c.video_id = #{videoId} " +
            "ORDER BY c.create_time DESC")
    List<Comment> selectCommentsByVideoId(Long videoId);
}

