-- V4__add_video_category_rating.sql
-- 为视频表添加分类和分级字段

ALTER TABLE `video_info` 
ADD COLUMN `category` varchar(50) DEFAULT 'Uncategorized' COMMENT '视频分类',
ADD COLUMN `rating` varchar(20) DEFAULT 'G' COMMENT '视频分级';

