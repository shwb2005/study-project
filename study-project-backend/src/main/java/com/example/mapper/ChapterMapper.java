package com.example.mapper;

import com.example.entity.Chapter;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface ChapterMapper {

    @Select("SELECT * FROM course_chapters WHERE course_id = #{courseId} ORDER BY sort_order ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "videoUrl", column = "video_url"),
            @Result(property = "sortOrder", column = "sort_order"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<Chapter> findByCourseId(@Param("courseId") Integer courseId);

    @Insert("INSERT INTO course_chapters (course_id, title, video_url, sort_order, created_at) " +
            "VALUES (#{courseId}, #{title}, #{videoUrl}, #{sortOrder}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Chapter chapter);

    @Update("UPDATE course_chapters SET title = #{title}, video_url = #{videoUrl}, " +
            "sort_order = #{sortOrder}, updated_at = NOW() WHERE id = #{id}")
    int update(Chapter chapter);

    @Delete("DELETE FROM course_chapters WHERE course_id = #{courseId}")
    int deleteByCourseId(@Param("courseId") Integer courseId);

    @Delete("DELETE FROM course_chapters WHERE id = #{id}")
    int deleteById(@Param("id") Integer id);

    @Update("UPDATE course_chapters SET sort_order = #{sortOrder}, updated_at = NOW() WHERE id = #{id}")
    void updateSortOrder(@Param("id") Integer id, @Param("sortOrder") Integer sortOrder);

    @Update("UPDATE course_chapters SET title = #{title}, video_url = #{videoUrl}, updated_at = NOW() WHERE id = #{id}")
    void updateChapterInfo(Chapter chapter);
}
