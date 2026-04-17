package com.example.service.study;

import com.example.entity.study.UserActivityLog;
import com.example.mapper.study.UserActivityLogMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityLogService {

    private static final Logger logger = LoggerFactory.getLogger(ActivityLogService.class);

    @Resource
    private UserActivityLogMapper userActivityLogMapper;

    public void log(Integer userId, String action, String description) {
        try {
            UserActivityLog activityLog = new UserActivityLog(userId, action, description);
            userActivityLogMapper.insert(activityLog);
        } catch (Exception e) {
            logger.error("记录活动日志失败: {}", e.getMessage(), e);
        }
    }

    public List<UserActivityLog> getUserLogs(Integer userId, int limit) {
        try {
            return userActivityLogMapper.findByUserId(userId, limit);
        } catch (Exception e) {
            logger.error("获取活动日志失败: {}", e.getMessage(), e);
            return List.of();
        }
    }
}
