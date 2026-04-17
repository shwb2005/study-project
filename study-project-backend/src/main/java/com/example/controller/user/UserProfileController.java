package com.example.controller.user;

import com.example.entity.RestBean;
import com.example.entity.user.UserProfile;
import com.example.entity.user.AccountUser;
import com.example.service.user.UserProfileService;
import com.example.service.study.ActivityLogService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    @Resource
    private UserProfileService userProfileService;

    @Resource
    private ActivityLogService activityLogService;

    @GetMapping
    public RestBean<UserProfile> getProfile(HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failureWithData(401, (UserProfile) null);
            }

            UserProfile profile = userProfileService.getUserProfile(user.getId());
            if (profile != null) {
                return RestBean.success(profile);
            } else {
                boolean initialized = userProfileService.initializeUserProfile(user.getId());
                if (initialized) {
                    profile = userProfileService.getUserProfile(user.getId());
                    return RestBean.success(profile);
                } else {
                    UserProfile emptyProfile = new UserProfile();
                    emptyProfile.setUserId(user.getId());
                    return RestBean.success(emptyProfile);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return RestBean.failureWithData(500, (UserProfile) null);
        }
    }

    @PostMapping
    public RestBean<String> updateProfile(@RequestBody UserProfile profile, HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }

            profile.setUserId(user.getId());

            if (profile.getGender() != null) {
                String gender = profile.getGender();
                if ("男".equals(gender)) {
                    profile.setGender("male");
                } else if ("女".equals(gender)) {
                    profile.setGender("female");
                } else if ("未知".equals(gender)) {
                    profile.setGender("unknown");
                }
            } else {
                profile.setGender("unknown");
            }

            boolean exists = userProfileService.existsUserProfile(user.getId());
            boolean success;

            if (exists) {
                success = userProfileService.updateUserProfile(profile);
            } else {
                success = userProfileService.createUserProfile(profile);
            }

            if (success) {
                activityLogService.log(user.getId(), "更新资料", "更新了个人资料");
                return RestBean.success("资料更新成功");
            } else {
                return RestBean.failure(500, "资料更新失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return RestBean.failure(500, "资料更新失败");
        }
    }

    @PostMapping("/initialize")
    public RestBean<String> initializeProfile(HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }

            boolean success = userProfileService.initializeUserProfile(user.getId());
            if (success) {
                return RestBean.success("用户档案初始化成功");
            } else {
                return RestBean.failure(500, "用户档案初始化失败");
            }
        } catch (Exception e) {
            return RestBean.failure(500, "初始化失败");
        }
    }
}
