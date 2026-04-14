package com.example.mapper;

import com.example.entity.Course;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CourseMapper {

    @Select("SELECT * FROM courses WHERE status = 'published' ORDER BY id DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "teacherName", column = "teacher_name"),
            @Result(property = "duration", column = "duration"),
            @Result(property = "studentsCount", column = "students_count"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "status", column = "status"),
            @Result(property = "maxCheckInCount", column = "max_check_in_count"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "videoUrl", column = "video_url")
    })
    List<Course> findAll();

    @Select("SELECT * FROM courses WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "teacherName", column = "teacher_name"),
            @Result(property = "duration", column = "duration"),
            @Result(property = "studentsCount", column = "students_count"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "status", column = "status"),
            @Result(property = "maxCheckInCount", column = "max_check_in_count"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "videoUrl", column = "video_url")
    })
    Course findById(Integer id);

    @Select("SELECT c.* FROM courses c " +
            "INNER JOIN user_course_relation ucr ON c.id = ucr.course_id " +
            "WHERE ucr.user_id = #{userId} ORDER BY ucr.enrolled_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "teacherName", column = "teacher_name"),
            @Result(property = "duration", column = "duration"),
            @Result(property = "studentsCount", column = "students_count"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "status", column = "status"),
            @Result(property = "maxCheckInCount", column = "max_check_in_count"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "videoUrl", column = "video_url")
    })
    List<Course> findByUserId(Integer userId);

    @Insert("INSERT INTO courses (name, description, teacher_name, duration, students_count, rating, status, max_check_in_count, cover_image, video_url) " +
            "VALUES (#{name}, #{description}, #{teacherName}, #{duration}, #{studentsCount}, #{rating}, #{status}, #{maxCheckInCount}, #{coverImage}, #{videoUrl})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertCourse(Course course);

    @Delete("DELETE FROM courses WHERE id = #{courseId}")
    int deleteCourse(Integer courseId);

    @Update("UPDATE courses SET " +
            "name = #{name}, " +
            "description = #{description}, " +
            "teacher_name = #{teacherName}, " +
            "duration = #{duration}, " +
            "students_count = #{studentsCount}, " +
            "max_check_in_count = #{maxCheckInCount}, " +
            "cover_image = #{coverImage}, " +
            "video_url = #{videoUrl} " +
            "WHERE id = #{id}")
    int updateCourse(Course course);
    
    @Select("<script>" +
            "SELECT * FROM courses WHERE status = 'published' " +
            "<if test='search != null and search != \"\"'>" +
            "AND name LIKE CONCAT('%', #{search}, '%') " +
            "</if>" +
            "<if test='teacherName != null and teacherName != \"\"'>" +
            "AND teacher_name LIKE CONCAT('%', #{teacherName}, '%') " +
            "</if>" +
            "ORDER BY " +
            "<choose>" +
            "<when test='sortBy == \"rating\"'>rating ${order}</when>" +
            "<when test='sortBy == \"students\"'>students_count ${order}</when>" +
            "<when test='sortBy == \"name\"'>name ${order}</when>" +
            "<when test='sortBy == \"duration\"'>duration ${order}</when>" +
            "<otherwise>id DESC</otherwise>" +
            "</choose>" +
            "<if test='page != null and pageSize != null'>" +
            "LIMIT #{page}, #{pageSize}" +
            "</if>" +
            "</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "teacherName", column = "teacher_name"),
            @Result(property = "duration", column = "duration"),
            @Result(property = "studentsCount", column = "students_count"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "status", column = "status"),
            @Result(property = "maxCheckInCount", column = "max_check_in_count"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "videoUrl", column = "video_url")
    })
    List<Course> findWithOptions(@Param("sortBy") String sortBy,
                                 @Param("order") String order,
                                 @Param("search") String search,
                                 @Param("teacherName") String teacherName,
                                 @Param("page") Integer page,
                                 @Param("pageSize") Integer pageSize);

    @Select("<script>" +
            "SELECT COUNT(*) FROM courses WHERE status = 'published' " +
            "<if test='search != null and search != \"\"'>" +
            "AND name LIKE CONCAT('%', #{search}, '%') " +
            "</if>" +
            "<if test='teacherName != null and teacherName != \"\"'>" +
            "AND teacher_name LIKE CONCAT('%', #{teacherName}, '%') " +
            "</if>" +
            "</script>")
    int countWithOptions(@Param("search") String search, @Param("teacherName") String teacherName);

    // 管理员：不过滤 status
    @Select("<script>" +
            "SELECT * FROM courses WHERE 1=1 " +
            "<if test='search != null and search != \"\"'>" +
            "AND name LIKE CONCAT('%', #{search}, '%') " +
            "</if>" +
            "ORDER BY id DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "teacherName", column = "teacher_name"),
            @Result(property = "duration", column = "duration"),
            @Result(property = "studentsCount", column = "students_count"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "status", column = "status"),
            @Result(property = "maxCheckInCount", column = "max_check_in_count"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "videoUrl", column = "video_url")
    })
    List<Course> findAdminPaged(@Param("search") String search, @Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("<script>" +
            "SELECT COUNT(*) FROM courses WHERE 1=1 " +
            "<if test='search != null and search != \"\"'>" +
            "AND name LIKE CONCAT('%', #{search}, '%') " +
            "</if>" +
            "</script>")
    int countAdmin(@Param("search") String search);
    
    @Select("SELECT DISTINCT teacher_name FROM courses WHERE status = 'published' ORDER BY teacher_name")
    List<String> findAllTeachers();
    
    @Select("SELECT * FROM courses WHERE status = 'published' AND teacher_name LIKE CONCAT('%', #{teacherName}, '%') ORDER BY id DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "teacherName", column = "teacher_name"),
            @Result(property = "duration", column = "duration"),
            @Result(property = "studentsCount", column = "students_count"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "status", column = "status"),
            @Result(property = "maxCheckInCount", column = "max_check_in_count"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "videoUrl", column = "video_url")
    })
    List<Course> findByTeacherName(String teacherName);
    
    @Select("SELECT * FROM courses WHERE status = 'published' ORDER BY id DESC LIMIT #{page}, #{pageSize}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "teacherName", column = "teacher_name"),
            @Result(property = "duration", column = "duration"),
            @Result(property = "studentsCount", column = "students_count"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "status", column = "status"),
            @Result(property = "maxCheckInCount", column = "max_check_in_count"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "videoUrl", column = "video_url")
    })
    List<Course> findPaginated(@Param("page") int page, @Param("pageSize") int pageSize);
    
    @Select("<script>" +
            "SELECT * FROM courses WHERE status = 'published' " +
            "ORDER BY " +
            "<choose>" +
            "<when test='sortBy == \"rating\"'>rating ${order}</when>" +
            "<when test='sortBy == \"students\"'>students_count ${order}</when>" +
            "<when test='sortBy == \"name\"'>name ${order}</when>" +
            "<when test='sortBy == \"duration\"'>duration ${order}</when>" +
            "<otherwise>id DESC</otherwise>" +
            "</choose>" +
            "</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "name", column = "name"),
            @Result(property = "description", column = "description"),
            @Result(property = "teacherName", column = "teacher_name"),
            @Result(property = "duration", column = "duration"),
            @Result(property = "studentsCount", column = "students_count"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "status", column = "status"),
            @Result(property = "maxCheckInCount", column = "max_check_in_count"),
            @Result(property = "coverImage", column = "cover_image"),
            @Result(property = "videoUrl", column = "video_url")
    })
    List<Course> findAllSorted(@Param("sortBy") String sortBy, @Param("order") String order);
}