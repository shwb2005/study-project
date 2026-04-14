package com.example.entity;

import java.util.List;
import java.util.Map;

public class UserStudyStats {
    private int enrolledCount;
    private int completedCount;
    private int totalCheckIns;
    private double avgProgress;
    private int ratingCount;
    private double avgRating;
    private int favoriteCount;
    private List<Map<String, Object>> courseProgressList;
    private List<Map<String, Object>> checkInTrend;

    public int getEnrolledCount() { return enrolledCount; }
    public void setEnrolledCount(int enrolledCount) { this.enrolledCount = enrolledCount; }
    public int getCompletedCount() { return completedCount; }
    public void setCompletedCount(int completedCount) { this.completedCount = completedCount; }
    public int getTotalCheckIns() { return totalCheckIns; }
    public void setTotalCheckIns(int totalCheckIns) { this.totalCheckIns = totalCheckIns; }
    public double getAvgProgress() { return avgProgress; }
    public void setAvgProgress(double avgProgress) { this.avgProgress = avgProgress; }
    public int getRatingCount() { return ratingCount; }
    public void setRatingCount(int ratingCount) { this.ratingCount = ratingCount; }
    public double getAvgRating() { return avgRating; }
    public void setAvgRating(double avgRating) { this.avgRating = avgRating; }
    public int getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(int favoriteCount) { this.favoriteCount = favoriteCount; }
    public List<Map<String, Object>> getCourseProgressList() { return courseProgressList; }
    public void setCourseProgressList(List<Map<String, Object>> courseProgressList) { this.courseProgressList = courseProgressList; }
    public List<Map<String, Object>> getCheckInTrend() { return checkInTrend; }
    public void setCheckInTrend(List<Map<String, Object>> checkInTrend) { this.checkInTrend = checkInTrend; }
}
