package com.combine.auditorium.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("video_comment")
public class Comment {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long videoId;
    private Long userId;
    private String content;
    private LocalDateTime createTime;
    
    // 非数据库字段，用于关联查询用户信息
    @TableField(exist = false)
    private String nickname;
    @TableField(exist = false)
    private String avatar;
}

