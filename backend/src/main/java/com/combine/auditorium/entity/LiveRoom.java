package com.combine.auditorium.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 直播间实体类
 */
@Data
@TableName("live_room")
public class LiveRoom {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String title;
    private String coverUrl;
    
    /**
     * 房间号 (公开，用于观众访问)
     */
    private String roomCode;
    
    /**
     * 推流密钥 (私密，用于主播推流鉴权)
     */
    private String streamKey;
    
    /**
     * 状态: 0-未开播, 1-直播中
     */
    private Integer status;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

