package com.example.mapper;

import com.example.entity.Course;
import com.example.entity.UserCourseRelation;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserCourseRelationMapper {

    @Select("SELECT * FROM user_course_relation WHERE user_id = #{userId} AND course_id = #{courseId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "progress", column = "progress"),
            @Result(property = "checkInCount", column = "check_in_count"),
            @Result(property = "maxCheckInCount", column = "max_check_in_count"),
            @Result(property = "lastCheckInDate", column = "last_check_in_date"),
            @Result(property = "currentLesson", column = "current_lesson"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "review", column = "review"),
            @Result(property = "isFavorite", column = "is_favorite"),
            @Result(property = "enrolledAt", column = "enrolled_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    UserCourseRelation findByUserIdAndCourseId(@Param("userId") Integer userId, @Param("courseId") Integer courseId);

    @Select("SELECT ucr.*, c.name as course_name, c.description, c.teacher_name, c.duration, c.students_count, c.rating as course_rating, c.max_check_in_count as course_max_check_in " +
            "FROM user_course_relation ucr " +
            "LEFT JOIN courses c ON ucr.course_id = c.id " +
            "WHERE ucr.user_id = #{userId} ORDER BY ucr.enrolled_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "progress", column = "progress"),
            @Result(property = "checkInCount", column = "check_in_count"),
            @Result(property = "maxCheckInCount", column = "max_check_in_count"),
            @Result(property = "lastCheckInDate", column = "last_check_in_date"),
            @Result(property = "currentLesson", column = "current_lesson"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "review", column = "review"),
            @Result(property = "isFavorite", column = "is_favorite"),
            @Result(property = "enrolledAt", column = "enrolled_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "course", column = "course_id", javaType = Course.class,
                    one = @One(select = "com.example.mapper.CourseMapper.findById"))
    })
    List<UserCourseRelation> findByUserIdWithCourse(@Param("userId") Integer userId);

    @Insert("INSERT INTO user_course_relation (user_id, course_id, progress, check_in_count, max_check_in_count, is_favorite, enrolled_at) " +
            "VALUES (#{userId}, #{courseId}, #{progress}, #{checkInCount}, #{maxCheckInCount}, #{isFavorite}, NOW())")
    int insert(UserCourseRelation relation);

    @Delete("DELETE FROM user_course_relation WHERE user_id = #{userId} AND course_id = #{courseId}")
    int deleteByUserIdAndCourseId(@Param("userId") Integer userId, @Param("courseId") Integer courseId);

    @Update("UPDATE user_course_relation SET rating = #{rating}, review = #{review}, updated_at = NOW() " +
            "WHERE user_id = #{userId} AND course_id = #{courseId}")
    int updateRating(UserCourseRelation relation);

    // 更新签到信息，包含最大签到次数
    @Update("UPDATE user_course_relation SET check_in_count = #{checkInCount}, " +
            "last_check_in_date = #{lastCheckInDate}, progress = #{progress}, " +
            "max_check_in_count = #{maxCheckInCount}, " +
            "updated_at = NOW() WHERE user_id = #{userId} AND course_id = #{courseId}")
    int updateCheckIn(UserCourseRelation relation);
    // 在 UserCourseRelationMapper 中添加
    @Delete("DELETE FROM user_course_relation WHERE course_id = #{courseId}")
    int deleteByCourseId(Integer courseId);
}