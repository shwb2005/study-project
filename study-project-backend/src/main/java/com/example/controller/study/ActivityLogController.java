package com.example.controller.study;

import com.example.entity.RestBean;
import com.example.entity.study.UserActivityLog;
import com.example.entity.user.AccountUser;
import com.example.service.study.ActivityLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/activity")
public class ActivityLogController {

    @Resource
    private ActivityLogService activityLogService;

    @GetMapping("/logs")
    public RestBean<List<UserActivityLog>> getLogs(
            @RequestParam(defaultValue = "10") int limit,
            HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, Collections.emptyList());
            }
            List<UserActivityLog> logs = activityLogService.getUserLogs(user.getId(), limit);
            return RestBean.success(logs);
        } catch (Exception e) {
            return RestBean.failure(500, Collections.emptyList());
        }
    }
}
