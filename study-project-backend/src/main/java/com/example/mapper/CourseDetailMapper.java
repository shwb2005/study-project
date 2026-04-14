package com.example.mapper;

import com.example.entity.CourseDetail;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CourseDetailMapper {

    @Select("SELECT * FROM course_detail WHERE course_id = #{courseId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "objectives", column = "objectives"),
            @Result(property = "outline", column = "outline"),
            @Result(property = "requirements", column = "requirements"),
            @Result(property = "audience", column = "audience"),
            @Result(property = "materials", column = "materials"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    CourseDetail findByCourseId(@Param("courseId") Integer courseId);

    @Insert("INSERT INTO course_detail (course_id, objectives, outline, requirements, audience, materials) " +
            "VALUES (#{courseId}, #{objectives}, #{outline}, #{requirements}, #{audience}, #{materials})")
    int insert(CourseDetail detail);

    @Update("UPDATE course_detail SET objectives = #{objectives}, outline = #{outline}, " +
            "requirements = #{requirements}, audience = #{audience}, materials = #{materials} " +
            "WHERE course_id = #{courseId}")
    int updateByCourseId(CourseDetail detail);
}
