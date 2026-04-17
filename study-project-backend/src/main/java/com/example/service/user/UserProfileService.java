package com.example.service.user;

import com.example.entity.user.UserProfile;

public interface UserProfileService {
    UserProfile getUserProfile(Integer userId);
    boolean updateUserProfile(UserProfile profile);
    boolean createUserProfile(UserProfile profile);
    boolean updateLastLoginTime(Integer userId);

    // 新增方法
    boolean initializeUserProfile(Integer userId);
    boolean existsUserProfile(Integer userId);
}