package com.combine.auditorium.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_config")
public class AIConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Double temperature;
    private Integer topK;
    private Integer knowledgeLimit;
    private LocalDateTime updatedAt;
}
