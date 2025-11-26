package com.combine.auditorium.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("video_info")
public class Video {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long userId;
    private String title;
    private String description;
    private String videoUrl;
    private String coverUrl;
    private String fileId; // 原始文件在MinIO中的路径
    
    /**
     * 状态: 0-转码中, 1-正常(上架), 2-失败, 3-下架
     */
    private Integer status;
    
    private Long duration; // 秒
    
    private String category; // 分类 (Genre): 剧情, 喜剧, etc.
    private String rating;   // 分级
    
    // 新增过滤字段
    private String videoType; // 类型: 电影, 电视剧, etc.
    private String region;    // 地区: 美国, 内地, etc.
    private Integer releaseYear; // 上映年份
    
    // 统计字段
    private Long viewCount;
    private Long commentCount;
    private Integer isHot; // 0-否, 1-是
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
