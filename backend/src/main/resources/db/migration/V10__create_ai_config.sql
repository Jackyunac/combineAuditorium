-- AI 参数配置表，仅一行配置全局对话策略
CREATE TABLE IF NOT EXISTS `ai_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `temperature` double DEFAULT 0.7 COMMENT '采样温度 0-1',
  `top_k` int DEFAULT 3 COMMENT 'TopK 取样',
  `knowledge_limit` int DEFAULT 3 COMMENT '知识库召回条数',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI 全局配置';

INSERT INTO ai_config (id, temperature, top_k, knowledge_limit)
VALUES (1, 0.7, 3, 3)
ON DUPLICATE KEY UPDATE temperature=VALUES(temperature), top_k=VALUES(top_k), knowledge_limit=VALUES(knowledge_limit);
