package com.example.entity;

import java.time.LocalDateTime;

public class CommunityReply {
    private Integer id;
    private Integer reviewId;
    private Integer userId;
    private String username;
    private String avatar;
    private String content;
    private Boolean isAnonymous;
    private Boolean isAuthorReply;
    private Boolean canDelete;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer parentReplyId;
    private Integer replyToUserId;
    private String replyToUsername;
    private Integer reviewUserId;
    private LocalDateTime createdAt;

    public CommunityReply() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getReviewId() { return reviewId; }
    public void setReviewId(Integer reviewId) { this.reviewId = reviewId; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Boolean getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Boolean isAnonymous) { this.isAnonymous = isAnonymous; }
    public Boolean getIsAuthorReply() { return isAuthorReply; }
    public void setIsAuthorReply(Boolean isAuthorReply) { this.isAuthorReply = isAuthorReply; }
    public Boolean getCanDelete() { return canDelete; }
    public void setCanDelete(Boolean canDelete) { this.canDelete = canDelete; }
    public Integer getLikeCount() { return likeCount; }
    public void setLikeCount(Integer likeCount) { this.likeCount = likeCount; }
    public Integer getDislikeCount() { return dislikeCount; }
    public void setDislikeCount(Integer dislikeCount) { this.dislikeCount = dislikeCount; }
    public Integer getParentReplyId() { return parentReplyId; }
    public void setParentReplyId(Integer parentReplyId) { this.parentReplyId = parentReplyId; }
    public Integer getReplyToUserId() { return replyToUserId; }
    public void setReplyToUserId(Integer replyToUserId) { this.replyToUserId = replyToUserId; }
    public String getReplyToUsername() { return replyToUsername; }
    public void setReplyToUsername(String replyToUsername) { this.replyToUsername = replyToUsername; }
    public Integer getReviewUserId() { return reviewUserId; }
    public void setReviewUserId(Integer reviewUserId) { this.reviewUserId = reviewUserId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
