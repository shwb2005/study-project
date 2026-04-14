package com.example.contorller;

import com.example.entity.RestBean;
import com.example.entity.UserProfile;
import com.example.entity.user.AccountUser;
import com.example.service.UserProfileService;
import com.example.service.ActivityLogService;
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

            System.out.println("=== 获取用户档案 ===");
            System.out.println("用户ID: " + user.getId());

            UserProfile profile = userProfileService.getUserProfile(user.getId());
            if (profile != null) {
                System.out.println("找到用户档案: " + profile);
                return RestBean.success(profile);
            } else {
                System.out.println("未找到用户档案，返回空对象");
                // 自动初始化用户档案
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
            System.out.println("获取用户档案异常: " + e.getMessage());
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

            System.out.println("=== 收到更新请求 ===");
            System.out.println("用户ID: " + user.getId());
            System.out.println("收到的数据: " + profile);

            // 确保只能修改自己的资料
            profile.setUserId(user.getId());

            // 【关键修改】转换 gender 值为 ENUM 允许的值
            if (profile.getGender() != null) {
                String gender = profile.getGender();
                System.out.println("原始 gender 值: " + gender);

                // 转换中文字符为 ENUM 值
                if ("男".equals(gender)) {
                    profile.setGender("male");
                } else if ("女".equals(gender)) {
                    profile.setGender("female");
                } else if ("未知".equals(gender)) {
                    profile.setGender("unknown");
                }
                // 如果不是以上中文字符，假设已经是正确的 ENUM 值 (male/female/unknown)

                System.out.println("转换后 gender 值: " + profile.getGender());
            } else {
                // 如果 gender 为 null，设置默认值
                profile.setGender("unknown");
            }

            // 检查是否已有个人资料
            boolean exists = userProfileService.existsUserProfile(user.getId());
            boolean success;

            if (exists) {
                // 更新现有资料
                success = userProfileService.updateUserProfile(profile);
                System.out.println("执行更新操作，结果: " + success);
            } else {
                // 创建新资料
                success = userProfileService.createUserProfile(profile);
                System.out.println("执行创建操作，结果: " + success);
            }

            if (success) {
                activityLogService.log(user.getId(), "更新资料", "更新了个人资料");
                return RestBean.success("资料更新成功");
            } else {
                return RestBean.failure(500, "资料更新失败");
            }
        } catch (Exception e) {
            System.out.println("更新异常: " + e.getMessage());
            e.printStackTrace();
            return RestBean.failure(500, "资料更新失败");
        }
    }

    // 新增：初始化用户档案接口
    @PostMapping("/initialize")
    public RestBean<String> initializeProfile(HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }

            System.out.println("=== 手动初始化用户档案 ===");
            System.out.println("用户ID: " + user.getId());

            boolean success = userProfileService.initializeUserProfile(user.getId());
            if (success) {
                return RestBean.success("用户档案初始化成功");
            } else {
                return RestBean.failure(500, "用户档案初始化失败");
            }
        } catch (Exception e) {
            System.out.println("初始化异常: " + e.getMessage());
            e.printStackTrace();
            return RestBean.failure(500, "初始化失败");
        }
    }
}