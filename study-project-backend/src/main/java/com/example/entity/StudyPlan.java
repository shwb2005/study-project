package com.example.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StudyPlan {
    private Integer id;
    private Integer userId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 瞬态字段
    private List<StudyPlanItem> items;
    private Integer itemCount;
    private Integer completedCount;

    public StudyPlan() {}

    // 显式 getter/setter，兼容 IDE 和 Lombok 未生效的情况
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public List<StudyPlanItem> getItems() { return items; }
    public void setItems(List<StudyPlanItem> items) { this.items = items; }
    public Integer getItemCount() { return itemCount; }
    public void setItemCount(Integer itemCount) { this.itemCount = itemCount; }
    public Integer getCompletedCount() { return completedCount; }
    public void setCompletedCount(Integer completedCount) { this.completedCount = completedCount; }

    @Override
    public String toString() {
        return "StudyPlan{" +
                "id=" + id +
                ", userId=" + userId +
                ", title='" + title + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status=" + status +
                '}';
    }
}
