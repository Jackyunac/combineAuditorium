-- Create tables for live room danmu and gift interactions

CREATE TABLE IF NOT EXISTS `live_danmu` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_code` varchar(50) NOT NULL COMMENT '直播间房间号',
  `user_id` bigint NOT NULL COMMENT '发送人用户ID',
  `content` varchar(255) NOT NULL COMMENT '弹幕内容',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`id`),
  KEY `idx_danmu_room_time` (`room_code`,`created_at`),
  KEY `idx_danmu_room_id` (`room_code`,`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播间弹幕';

CREATE TABLE IF NOT EXISTS `live_gift` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `room_code` varchar(50) NOT NULL COMMENT '直播间房间号',
  `user_id` bigint NOT NULL COMMENT '送礼人用户ID',
  `gift_type` varchar(50) NOT NULL COMMENT '礼物类型',
  `gift_count` int NOT NULL DEFAULT 1 COMMENT '礼物数量',
  `message` varchar(255) DEFAULT NULL COMMENT '附言',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '送礼时间',
  PRIMARY KEY (`id`),
  KEY `idx_gift_room_time` (`room_code`,`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='直播间礼物记录';
