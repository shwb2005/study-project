package com.example.service.impl;

import com.example.entity.UserProfile;
import com.example.mapper.UserProfileMapper;
import com.example.service.UserProfileService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Resource
    private UserProfileMapper userProfileMapper;

    @Override
    public UserProfile getUserProfile(Integer userId) {
        try {
            System.out.println("=== 查询用户档案 ===");
            System.out.println("用户ID: " + userId);

            UserProfile profile = userProfileMapper.findByUserId(userId);
            if (profile != null) {
                System.out.println("✅ 找到用户档案: ID=" + profile.getId());
            } else {
                System.out.println("❌ 未找到用户档案");
            }
            return profile;
        } catch (Exception e) {
            System.out.println("❌ 查询用户档案异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateUserProfile(UserProfile profile) {
        try {
            System.out.println("=== 执行更新操作 ===");
            System.out.println("用户ID: " + profile.getUserId());
            System.out.println("realName: " + profile.getRealName());
            System.out.println("phone: " + profile.getPhone());
            System.out.println("gender: " + profile.getGender());
            System.out.println("bio: " + profile.getBio());

            int result = userProfileMapper.update(profile);
            System.out.println("更新影响行数: " + result);

            boolean success = result > 0;
            if (success) {
                System.out.println("✅ 用户档案更新成功");
            } else {
                System.out.println("❌ 用户档案更新失败");
            }
            return success;
        } catch (Exception e) {
            System.out.println("❌ 更新用户档案异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean createUserProfile(UserProfile profile) {
        try {
            System.out.println("=== 执行创建操作 ===");
            System.out.println("用户ID: " + profile.getUserId());
            System.out.println("realName: " + profile.getRealName());
            System.out.println("gender: " + profile.getGender());

            // 设置默认值 - 使用 ENUM 允许的值
            if (profile.getRealName() == null) profile.setRealName("");
            if (profile.getAvatar() == null) profile.setAvatar("/default-avatar.png");
            if (profile.getPhone() == null) profile.setPhone("");
            if (profile.getGender() == null) profile.setGender("unknown"); // 使用 ENUM 值
            if (profile.getBio() == null) profile.setBio("这个人很懒，什么都没有写～");

            // 确保 gender 是有效的 ENUM 值
            String gender = profile.getGender();
            if (!"male".equals(gender) && !"female".equals(gender) && !"unknown".equals(gender)) {
                profile.setGender("unknown");
            }

            int result = userProfileMapper.insert(profile);
            System.out.println("创建影响行数: " + result);

            boolean success = result > 0;
            if (success) {
                System.out.println("✅ 用户档案创建成功");
            } else {
                System.out.println("❌ 用户档案创建失败");
            }
            return success;
        } catch (Exception e) {
            System.out.println("❌ 创建用户档案异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateLastLoginTime(Integer userId) {
        try {
            System.out.println("=== 更新最后登录时间 ===");
            System.out.println("用户ID: " + userId);

            int result = userProfileMapper.updateLoginTime(userId);
            System.out.println("更新最后登录时间影响行数: " + result);

            boolean success = result > 0;
            if (success) {
                System.out.println("✅ 最后登录时间更新成功");
            } else {
                System.out.println("❌ 最后登录时间更新失败");
            }
            return success;
        } catch (Exception e) {
            System.out.println("❌ 更新最后登录时间异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean initializeUserProfile(Integer userId) {
        try {
            // 检查是否已存在
            if (existsUserProfile(userId)) {
                System.out.println("✅ 用户档案已存在，用户ID: " + userId);
                return true;
            }

            // 创建默认档案
            UserProfile profile = new UserProfile();
            profile.setUserId(userId);

            boolean success = createUserProfile(profile);
            System.out.println("初始化用户档案结果: " + success + ", 用户ID: " + userId);

            if (success) {
                System.out.println("✅ 用户档案初始化成功");
            } else {
                System.out.println("❌ 用户档案初始化失败");
            }
            return success;
        } catch (Exception e) {
            System.out.println("❌ 初始化用户档案异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean existsUserProfile(Integer userId) {
        try {
            UserProfile profile = getUserProfile(userId);
            boolean exists = profile != null;
            System.out.println("检查用户档案是否存在: " + exists + ", 用户ID: " + userId);
            return exists;
        } catch (Exception e) {
            System.out.println("❌ 检查用户档案存在性异常: " + e.getMessage());
            return false;
        }
    }
}