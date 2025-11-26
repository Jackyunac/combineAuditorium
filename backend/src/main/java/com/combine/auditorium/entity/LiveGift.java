package com.combine.auditorium.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("live_gift")
public class LiveGift {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String roomCode;
    private Long userId;
    private String giftType;
    private Integer giftCount;
    private String message;
    private LocalDateTime createdAt;

    @TableField(exist = false)
    private String nickname;

    @TableField(exist = false)
    private String avatar;
}
