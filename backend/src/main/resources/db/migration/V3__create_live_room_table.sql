-- V3__create_live_room_table.sql
-- 创建直播间表

CREATE TABLE IF NOT EXISTS `live_room` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint NOT NULL COMMENT '主播ID (用户ID)',
  `title` varchar(255) NOT NULL COMMENT '直播间标题',
  `cover_url` varchar(500) DEFAULT NULL COMMENT '直播间封面',
  `room_code` varchar(50) NOT NULL COMMENT '房间号 (唯一标识)',
  `stream_key` varchar(100) NOT NULL COMMENT '推流密钥 (鉴权用)',
  `status` int NOT NULL DEFAULT 0 COMMENT '状态: 0-未开播, 1-直播中',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_room_code` (`room_code`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播间表';

