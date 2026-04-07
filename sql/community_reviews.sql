-- =============================================
-- 社区评价表：建表 + 历史数据迁移 + 触发器
-- =============================================

-- 1. 建表
CREATE TABLE IF NOT EXISTS `community_reviews` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '用户ID',
  `username` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `avatar` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `course_id` int NOT NULL COMMENT '课程ID',
  `course_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '课程名称',
  `rating` int NOT NULL COMMENT '评分(1-5)',
  `review` text COLLATE utf8mb4_unicode_ci COMMENT '评价内容',
  `is_anonymous` tinyint(1) DEFAULT 0 COMMENT '是否匿名',
  `created_at` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_course` (`user_id`, `course_id`),
  KEY `idx_course_id` (`course_id`),
  KEY `idx_rating` (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 历史数据迁移（仅执行一次）
INSERT INTO community_reviews (user_id, username, avatar, course_id, course_name, rating, review, created_at, updated_at)
SELECT ucr.user_id, da.username, up.avatar, ucr.course_id, c.name, ucr.rating, ucr.review, ucr.updated_at, ucr.updated_at
FROM user_course_relation ucr
JOIN db_account da ON ucr.user_id = da.id
LEFT JOIN user_profile up ON ucr.user_id = up.user_id
JOIN courses c ON ucr.course_id = c.id
WHERE ucr.rating IS NOT NULL;

-- 3. 触发器：user_course_relation INSERT 后同步到 community_reviews
DELIMITER ;;
CREATE TRIGGER `sync_community_on_insert`
AFTER INSERT ON `user_course_relation` FOR EACH ROW
BEGIN
    IF NEW.rating IS NOT NULL THEN
        INSERT INTO community_reviews (user_id, username, avatar, course_id, course_name, rating, review, created_at, updated_at)
        SELECT NEW.user_id, da.username, up.avatar, NEW.course_id, c.name, NEW.rating, NEW.review, NOW(), NOW()
        FROM db_account da
        LEFT JOIN user_profile up ON NEW.user_id = up.user_id
        JOIN courses c ON NEW.course_id = c.id
        WHERE da.id = NEW.user_id
        ON DUPLICATE KEY UPDATE
            rating = VALUES(rating),
            review = VALUES(review),
            updated_at = NOW();
    END IF;
END;;

-- 4. 触发器：user_course_relation UPDATE 后同步到 community_reviews
CREATE TRIGGER `sync_community_on_update`
AFTER UPDATE ON `user_course_relation` FOR EACH ROW
BEGIN
    IF NEW.rating IS NOT NULL THEN
        INSERT INTO community_reviews (user_id, username, avatar, course_id, course_name, rating, review, created_at, updated_at)
        SELECT NEW.user_id, da.username, up.avatar, NEW.course_id, c.name, NEW.rating, NEW.review, NOW(), NOW()
        FROM db_account da
        LEFT JOIN user_profile up ON NEW.user_id = up.user_id
        JOIN courses c ON NEW.course_id = c.id
        WHERE da.id = NEW.user_id
        ON DUPLICATE KEY UPDATE
            rating = VALUES(rating),
            review = VALUES(review),
            username = VALUES(username),
            avatar = VALUES(avatar),
            course_name = VALUES(course_name),
            updated_at = NOW();
    END IF;
END;;
DELIMITER ;

-- 是否匿名
ALTER TABLE community_reviews ADD COLUMN is_anonymous tinyint(1) DEFAULT 0 COMMENT '是否匿名' AFTER review;

