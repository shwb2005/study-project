package com.example.entity.user;

import com.example.entity.course.Course;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserCourseRelation {
    private Integer id;
    private Integer userId;
    private Integer courseId;
    private Integer progress;
    private String currentLesson;
    private Integer totalStudyTime;
    private Boolean isFavorite;
    private Integer rating;
    private String review;
    private LocalDateTime enrolledAt;
    private LocalDateTime lastStudyTime;
    private LocalDateTime completedAt;
    private LocalDateTime updatedAt;

    public UserCourseRelation() {

    }

    @Override
    public String toString() {
        return "UserCourseRelation{" +
                "id=" + id +
                ", userId=" + userId +
                ", courseId=" + courseId +
                ", progress=" + progress +
                ", currentLesson='" + currentLesson + '\'' +
                ", totalStudyTime=" + totalStudyTime +
                ", isFavorite=" + isFavorite +
                ", rating=" + rating +
                ", review='" + review + '\'' +
                ", enrolledAt=" + enrolledAt +
                ", lastStudyTime=" + lastStudyTime +
                ", completedAt=" + completedAt +
                ", updatedAt=" + updatedAt +
                ", course=" + course +
                '}';
    }

    public UserCourseRelation(Integer id, Integer userId, Integer courseId, Integer progress, String currentLesson, Boolean isFavorite, Integer totalStudyTime, Integer rating, String review, LocalDateTime enrolledAt, LocalDateTime lastStudyTime, LocalDateTime completedAt, LocalDateTime updatedAt, Course course) {
        this.id = id;
        this.userId = userId;
        this.courseId = courseId;
        this.progress = progress;
        this.currentLesson = currentLesson;
        this.isFavorite = isFavorite;
        this.totalStudyTime = totalStudyTime;
        this.rating = rating;
        this.review = review;
        this.enrolledAt = enrolledAt;
        this.lastStudyTime = lastStudyTime;
        this.completedAt = completedAt;
        this.updatedAt = updatedAt;
        this.course = course;
    }

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

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public String getCurrentLesson() {
        return currentLesson;
    }

    public void setCurrentLesson(String currentLesson) {
        this.currentLesson = currentLesson;
    }

    public Integer getTotalStudyTime() {
        return totalStudyTime;
    }

    public void setTotalStudyTime(Integer totalStudyTime) {
        this.totalStudyTime = totalStudyTime;
    }

    public Boolean getFavorite() {
        return isFavorite;
    }

    public void setFavorite(Boolean favorite) {
        isFavorite = favorite;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReview() {
        return review;
    }

    public void setReview(String review) {
        this.review = review;
    }

    public LocalDateTime getEnrolledAt() {
        return enrolledAt;
    }

    public void setEnrolledAt(LocalDateTime enrolledAt) {
        this.enrolledAt = enrolledAt;
    }

    public LocalDateTime getLastStudyTime() {
        return lastStudyTime;
    }

    public void setLastStudyTime(LocalDateTime lastStudyTime) {
        this.lastStudyTime = lastStudyTime;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public void setIsFavorite(Boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    // 关联字段
    private Course course;

    private Integer checkInCount; // 签到次数
    private LocalDateTime lastCheckInDate; // 最后签到日期

    // 添加 getter 和 setter 方法
    public Integer getCheckInCount() {
        return checkInCount;
    }

    public void setCheckInCount(Integer checkInCount) {
        this.checkInCount = checkInCount;
    }

    public LocalDateTime getLastCheckInDate() {
        return lastCheckInDate;
    }

    public void setLastCheckInDate(LocalDateTime lastCheckInDate) {
        this.lastCheckInDate = lastCheckInDate;
    }
    private Integer maxCheckInCount; // 课程最大签到次数

    // 添加 getter 和 setter
    public Integer getMaxCheckInCount() {
        return maxCheckInCount;
    }

    public void setMaxCheckInCount(Integer maxCheckInCount) {
        this.maxCheckInCount = maxCheckInCount;
    }
}



