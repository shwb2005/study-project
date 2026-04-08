-- =============================================
-- 社区互动功能：点赞/点踩 + 回复评价
-- =============================================

-- 1. 点赞/点踩记录表
CREATE TABLE IF NOT EXISTS `community_review_likes` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL,
  `review_id` int NOT NULL,
  `type` tinyint NOT NULL COMMENT '1=点赞 2=点踩',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_review` (`user_id`, `review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 回复评价表
CREATE TABLE IF NOT EXISTS `community_replies` (
  `id` int NOT NULL AUTO_INCREMENT,
  `review_id` int NOT NULL COMMENT '回复的评价ID',
  `user_id` int NOT NULL,
  `username` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `is_anonymous` tinyint DEFAULT 0,
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_review_id` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. community_reviews 加点赞/点踩计数字段
ALTER TABLE community_reviews ADD COLUMN `like_count` int DEFAULT 0 AFTER `is_anonymous`;
ALTER TABLE community_reviews ADD COLUMN `dislike_count` int DEFAULT 0 AFTER `like_count`;
