package com.example.mapper;

import com.example.entity.Course;
import com.example.entity.UserCourseFavorite;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserCourseFavoriteMapper {

    @Select("SELECT * FROM user_course_favorite WHERE user_id = #{userId} AND course_id = #{courseId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "createdAt", column = "created_at")
    })
    UserCourseFavorite findByUserIdAndCourseId(@Param("userId") Integer userId, @Param("courseId") Integer courseId);

    @Select("SELECT f.*, c.id as c_id, c.name as c_name, c.description as c_desc, " +
            "c.teacher_name as c_teacher, c.duration as c_duration, " +
            "c.students_count as c_students, c.rating as c_rating, c.max_check_in_count as c_max_checkin " +
            "FROM user_course_favorite f " +
            "LEFT JOIN courses c ON f.course_id = c.id " +
            "WHERE f.user_id = #{userId} ORDER BY f.created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "course", column = "course_id", javaType = Course.class,
                    one = @One(select = "com.example.mapper.CourseMapper.findById"))
    })
    List<UserCourseFavorite> findFavoritesByUserId(@Param("userId") Integer userId);

    @Insert("INSERT INTO user_course_favorite (user_id, course_id, created_at) " +
            "VALUES (#{userId}, #{courseId}, NOW())")
    int insert(UserCourseFavorite favorite);

    @Delete("DELETE FROM user_course_favorite WHERE user_id = #{userId} AND course_id = #{courseId}")
    int deleteByUserIdAndCourseId(@Param("userId") Integer userId, @Param("courseId") Integer courseId);

    @Select("SELECT course_id FROM user_course_favorite WHERE user_id = #{userId}")
    List<Integer> findFavoriteCourseIdsByUserId(@Param("userId") Integer userId);

    @Select("SELECT COUNT(*) FROM user_course_favorite WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Integer userId);

    @Select("SELECT f.*, c.id as c_id, c.name as c_name, c.description as c_desc, " +
            "c.teacher_name as c_teacher, c.duration as c_duration, " +
            "c.students_count as c_students, c.rating as c_rating, c.max_check_in_count as c_max_checkin " +
            "FROM user_course_favorite f " +
            "LEFT JOIN courses c ON f.course_id = c.id " +
            "WHERE f.user_id = #{userId} ORDER BY f.created_at DESC " +
            "LIMIT #{offset}, #{pageSize}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "course", column = "course_id", javaType = Course.class,
                    one = @One(select = "com.example.mapper.CourseMapper.findById"))
    })
    List<UserCourseFavorite> findFavoritesByUserIdPaged(@Param("userId") Integer userId, @Param("offset") int offset, @Param("pageSize") int pageSize);
}
