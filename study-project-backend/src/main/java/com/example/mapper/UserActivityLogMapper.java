package com.example.mapper;

import com.example.entity.UserActivityLog;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserActivityLogMapper {

    @Insert("INSERT INTO user_activity_log (user_id, action, description, created_at) " +
            "VALUES (#{userId}, #{action}, #{description}, NOW())")
    int insert(UserActivityLog log);

    @Select("SELECT * FROM user_activity_log WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC LIMIT #{limit}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "action", column = "action"),
            @Result(property = "description", column = "description"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<UserActivityLog> findByUserId(@Param("userId") Integer userId, @Param("limit") int limit);

    @Select("SELECT DATE(created_at) as date, COUNT(*) as count " +
            "FROM user_activity_log " +
            "WHERE user_id = #{userId} AND action = '课程签到' " +
            "AND created_at >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) " +
            "GROUP BY DATE(created_at) ORDER BY date")
    List<Map<String, Object>> findCheckInTrend(@Param("userId") Integer userId);

    @Delete("DELETE FROM user_activity_log WHERE created_at < DATE_SUB(NOW(), INTERVAL 3 DAY)")
    int deleteOlderThan3Days();
}
