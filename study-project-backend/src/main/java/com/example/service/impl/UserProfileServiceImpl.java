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
            return userProfileMapper.findByUserId(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean updateUserProfile(UserProfile profile) {
        try {
            int result = userProfileMapper.update(profile);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean createUserProfile(UserProfile profile) {
        try {
            if (profile.getRealName() == null) profile.setRealName("");
            if (profile.getAvatar() == null) profile.setAvatar("/default-avatar.png");
            if (profile.getPhone() == null) profile.setPhone("");
            if (profile.getGender() == null) profile.setGender("unknown");
            if (profile.getBio() == null) profile.setBio("这个人很懒，什么都没有写～");

            String gender = profile.getGender();
            if (!"male".equals(gender) && !"female".equals(gender) && !"unknown".equals(gender)) {
                profile.setGender("unknown");
            }

            int result = userProfileMapper.insert(profile);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateLastLoginTime(Integer userId) {
        try {
            int result = userProfileMapper.updateLoginTime(userId);
            return result > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    @Transactional
    public boolean initializeUserProfile(Integer userId) {
        try {
            if (existsUserProfile(userId)) {
                return true;
            }
            UserProfile profile = new UserProfile();
            profile.setUserId(userId);
            return createUserProfile(profile);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean existsUserProfile(Integer userId) {
        try {
            return getUserProfile(userId) != null;
        } catch (Exception e) {
            return false;
        }
    }
}
