package com.example.service;

import com.example.mapper.UserActivityLogMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ActivityLogCleanupTask {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLogCleanupTask.class);

    @Resource
    private UserActivityLogMapper userActivityLogMapper;

    @Scheduled(cron = "0 0 3 * * *")
    public void cleanOldLogs() {
        try {
            int rows = userActivityLogMapper.deleteOlderThan3Days();
            logger.info("清理3天前的活动日志，删除 {} 条", rows);
        } catch (Exception e) {
            logger.error("清理活动日志失败: {}", e.getMessage(), e);
        }
    }
}
