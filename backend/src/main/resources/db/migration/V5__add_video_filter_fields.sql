ALTER TABLE video_info ADD COLUMN video_type VARCHAR(50) COMMENT '视频类型: 电影, 电视剧, 综艺, etc.';
ALTER TABLE video_info ADD COLUMN region VARCHAR(50) COMMENT '地区: 美国, 内地, etc.';
ALTER TABLE video_info ADD COLUMN release_year INT COMMENT '上映年份';

