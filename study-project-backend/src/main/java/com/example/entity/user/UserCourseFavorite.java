package com.example.entity.user;

import com.example.entity.course.Course;
import java.time.LocalDateTime;

public class UserCourseFavorite {
    private Integer id;
    private Integer userId;
    private Integer courseId;
    private LocalDateTime createdAt;

    // 关联课程信息（JOIN 查询时填充）
    private Course course;

    public UserCourseFavorite() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
