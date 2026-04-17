package com.example.entity.study;

import java.time.LocalDateTime;

public class UserActivityLog {
    private Integer id;
    private Integer userId;
    private String action;
    private String description;
    private LocalDateTime createdAt;

    public UserActivityLog() {}

    public UserActivityLog(Integer userId, String action, String description) {
        this.userId = userId;
        this.action = action;
        this.description = description;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
