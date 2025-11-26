-- 增加视频统计和热门字段
ALTER TABLE video_info ADD COLUMN view_count BIGINT DEFAULT 0 COMMENT '播放量';
ALTER TABLE video_info ADD COLUMN comment_count BIGINT DEFAULT 0 COMMENT '评论数';
ALTER TABLE video_info ADD COLUMN is_hot TINYINT(1) DEFAULT 0 COMMENT '是否热门';

-- 创建评论表
CREATE TABLE video_comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    video_id BIGINT NOT NULL COMMENT '视频ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    content TEXT NOT NULL COMMENT '评论内容',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_video_id (video_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频评论表';

