-- 社区分区：添加 type 字段区分课程评价和讨论
-- type = 0 (或 NULL) = 课程评价（有星级评分）
-- type = 1 = 讨论（纯文字，无评分）

ALTER TABLE `community_reviews`
  ADD COLUMN `type` tinyint(1) DEFAULT 0 COMMENT '0=课程评价 1=讨论' AFTER `is_anonymous`;

-- 已有数据默认为评价类型
UPDATE `community_reviews` SET `type` = 0 WHERE `type` IS NULL;

-- 删除旧的唯一约束（每用户每课程只能有一条评价）
-- 讨论类型需要允许同一用户对同一课程发起多条讨论
ALTER TABLE `community_reviews` DROP INDEX `uk_user_course`;
