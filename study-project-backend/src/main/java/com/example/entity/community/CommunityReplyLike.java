package com.example.entity.community;

import java.time.LocalDateTime;

public class CommunityReplyLike {
    private Integer id;
    private Integer userId;
    private Integer replyId;
    private Integer type; // 1=点赞 2=点踩
    private LocalDateTime createdAt;

    public CommunityReplyLike() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public Integer getReplyId() { return replyId; }
    public void setReplyId(Integer replyId) { this.replyId = replyId; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
