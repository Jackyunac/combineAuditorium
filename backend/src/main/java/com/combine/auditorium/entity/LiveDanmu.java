package com.combine.auditorium.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("live_danmu")
public class LiveDanmu {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String roomCode;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String nickname;

    @TableField(exist = false)
    private String avatar;
}
