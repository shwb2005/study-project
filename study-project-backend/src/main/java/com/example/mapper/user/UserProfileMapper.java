package com.example.mapper.user;

import com.example.entity.user.UserProfile;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserProfileMapper {

    @Select("SELECT * FROM user_profile WHERE user_id = #{userId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "realName", column = "real_name"),
            @Result(property = "avatar", column = "avatar"),
            @Result(property = "phone", column = "phone"),
            @Result(property = "gender", column = "gender"),
            @Result(property = "birthday", column = "birthday"),
            @Result(property = "bio", column = "bio"),
            @Result(property = "lastLoginTime", column = "last_login_time"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    UserProfile findByUserId(Integer userId);

    // 根据您的数据库结构，只插入存在的字段
    @Insert("INSERT INTO user_profile (user_id, real_name, avatar, phone, gender, birthday, bio) " +
            "VALUES (#{userId}, #{realName}, #{avatar}, #{phone}, #{gender}, #{birthday}, #{bio})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserProfile profile);

    @Update("UPDATE user_profile SET " +
            "real_name = #{realName}, " +
            "avatar = #{avatar}, " +
            "phone = #{phone}, " +
            "gender = #{gender}, " +
            "birthday = #{birthday}, " +
            "bio = #{bio}, " +
            "updated_at = NOW() " +
            "WHERE user_id = #{userId}")
    int update(UserProfile profile);

    @Update("UPDATE user_profile SET last_login_time = NOW() WHERE user_id = #{userId}")
    int updateLoginTime(Integer userId);

    // 新增：检查用户档案是否存在
    @Select("SELECT COUNT(*) FROM user_profile WHERE user_id = #{userId}")
    boolean existsByUserId(Integer userId);
}