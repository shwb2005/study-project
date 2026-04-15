package com.example.mapper;

import com.example.entity.StudyPlan;
import com.example.entity.StudyPlanItem;
import org.apache.ibatis.annotations.*;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StudyPlanMapper {

    // ==================== 计划操作 ====================

    @Select("SELECT * FROM study_plans WHERE user_id = #{userId} ORDER BY start_date DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "description", column = "description"),
            @Result(property = "startDate", column = "start_date"),
            @Result(property = "endDate", column = "end_date"),
            @Result(property = "status", column = "status"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<StudyPlan> findByUserId(@Param("userId") Integer userId);

    @Select("SELECT * FROM study_plans WHERE id = #{id} AND user_id = #{userId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "description", column = "description"),
            @Result(property = "startDate", column = "start_date"),
            @Result(property = "endDate", column = "end_date"),
            @Result(property = "status", column = "status"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    StudyPlan findByIdAndUserId(@Param("id") Integer id, @Param("userId") Integer userId);

    @Select("<script>" +
            "SELECT * FROM study_plans WHERE user_id = #{userId} " +
            "ORDER BY start_date DESC " +
            "LIMIT #{offset}, #{pageSize}" +
            "</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "title", column = "title"),
            @Result(property = "description", column = "description"),
            @Result(property = "startDate", column = "start_date"),
            @Result(property = "endDate", column = "end_date"),
            @Result(property = "status", column = "status"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<StudyPlan> findByUserIdPaged(@Param("userId") Integer userId,
                                      @Param("offset") int offset,
                                      @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM study_plans WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Integer userId);

    @Insert("INSERT INTO study_plans (user_id, title, description, start_date, end_date, status, created_at) " +
            "VALUES (#{userId}, #{title}, #{description}, #{startDate}, #{endDate}, #{status}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(StudyPlan plan);

    @Update("UPDATE study_plans SET title = #{title}, description = #{description}, " +
            "start_date = #{startDate}, end_date = #{endDate}, status = #{status}, " +
            "updated_at = NOW() WHERE id = #{id} AND user_id = #{userId}")
    int update(StudyPlan plan);

    @Delete("DELETE FROM study_plans WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Integer id, @Param("userId") Integer userId);

    // ==================== 任务项操作 ====================

    @Select("SELECT spi.*, c.name as course_name FROM study_plan_items spi " +
            "LEFT JOIN courses c ON spi.course_id = c.id " +
            "WHERE spi.plan_id = #{planId} " +
            "ORDER BY spi.plan_date ASC, spi.sort_order ASC, spi.start_time ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "planId", column = "plan_id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "customTopic", column = "custom_topic"),
            @Result(property = "planDate", column = "plan_date"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "notes", column = "notes"),
            @Result(property = "completed", column = "completed"),
            @Result(property = "sortOrder", column = "sort_order"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "courseName", column = "course_name")
    })
    List<StudyPlanItem> findItemsByPlanId(@Param("planId") Integer planId);

    @Insert("INSERT INTO study_plan_items (plan_id, course_id, custom_topic, plan_date, start_time, end_time, " +
            "notes, completed, sort_order, created_at) " +
            "VALUES (#{planId}, #{courseId}, #{customTopic}, #{planDate}, #{startTime}, #{endTime}, " +
            "#{notes}, #{completed}, #{sortOrder}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertItem(StudyPlanItem item);

    @Update("UPDATE study_plan_items SET course_id = #{courseId}, custom_topic = #{customTopic}, " +
            "plan_date = #{planDate}, start_time = #{startTime}, end_time = #{endTime}, " +
            "notes = #{notes}, completed = #{completed}, sort_order = #{sortOrder}, " +
            "updated_at = NOW() WHERE id = #{id} AND plan_id = #{planId}")
    int updateItem(StudyPlanItem item);

    @Delete("DELETE FROM study_plan_items WHERE id = #{id} AND plan_id = #{planId}")
    int deleteItem(@Param("id") Integer id, @Param("planId") Integer planId);

    @Update("UPDATE study_plan_items SET completed = #{completed}, updated_at = NOW() WHERE id = #{id}")
    int updateItemCompletion(@Param("id") Integer id, @Param("completed") boolean completed);

    // ==================== 日历视图：跨表 JOIN 查询 ====================

    @Select("SELECT spi.*, c.name as course_name FROM study_plan_items spi " +
            "LEFT JOIN courses c ON spi.course_id = c.id " +
            "INNER JOIN study_plans sp ON spi.plan_id = sp.id " +
            "WHERE sp.user_id = #{userId} AND spi.plan_date BETWEEN #{startDate} AND #{endDate} " +
            "ORDER BY spi.plan_date ASC, spi.start_time ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "planId", column = "plan_id"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "customTopic", column = "custom_topic"),
            @Result(property = "planDate", column = "plan_date"),
            @Result(property = "startTime", column = "start_time"),
            @Result(property = "endTime", column = "end_time"),
            @Result(property = "notes", column = "notes"),
            @Result(property = "completed", column = "completed"),
            @Result(property = "sortOrder", column = "sort_order"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at"),
            @Result(property = "courseName", column = "course_name")
    })
    List<StudyPlanItem> findItemsByUserIdAndDateRange(
            @Param("userId") Integer userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
