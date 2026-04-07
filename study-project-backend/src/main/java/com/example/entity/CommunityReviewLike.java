package com.example.entity;

import java.time.LocalDateTime;

public class CommunityReviewLike {
    private Integer id;
    private Integer userId;
    private Integer reviewId;
    private Integer type; // 1=点赞 2=点踩
    private LocalDateTime createdAt;

    public CommunityReviewLike() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getReviewId() { return reviewId; }
    public void setReviewId(Integer reviewId) { this.reviewId = reviewId; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
