-- V2__create_video_table.sql
-- 创建视频信息表

CREATE TABLE IF NOT EXISTS `video_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '上传者ID',
  `title` varchar(255) NOT NULL COMMENT '视频标题',
  `description` text DEFAULT NULL COMMENT '视频描述',
  `video_url` varchar(500) DEFAULT NULL COMMENT '原视频/转码后播放地址',
  `cover_url` varchar(500) DEFAULT NULL COMMENT '封面图地址',
  `file_id` varchar(100) DEFAULT NULL COMMENT 'MinIO文件ID/路径',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态: 0-转码中, 1-正常, 2-失败',
  `duration` bigint DEFAULT 0 COMMENT '时长(秒)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='视频信息表';

