-- 回复点赞/点踩表
CREATE TABLE `community_reply_likes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `reply_id` int NOT NULL,
  `type` tinyint NOT NULL COMMENT '1=点赞 2=点踩',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_reply` (`user_id`, `reply_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='回复点赞/点踩记录';

-- 修改 community_replies 表，添加点赞计数和二级回复支持
ALTER TABLE `community_replies`
ADD COLUMN `like_count` int DEFAULT 0 COMMENT '点赞数',
ADD COLUMN `dislike_count` int DEFAULT 0 COMMENT '点踩数',
ADD COLUMN `parent_reply_id` int DEFAULT NULL COMMENT '父回复ID，NULL表示直接回复评价',
ADD COLUMN `reply_to_user_id` int DEFAULT NULL COMMENT '被回复用户ID',
ADD COLUMN `reply_to_username` varchar(100) DEFAULT NULL COMMENT '被回复用户名',
ADD KEY `idx_parent_reply` (`parent_reply_id`);
