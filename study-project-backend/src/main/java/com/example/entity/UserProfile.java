package com.example.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Date;

@Data
public class UserProfile {
    private Integer id;
    private Integer userId;

    @JsonProperty("real_name")
    private String realName;

    private String avatar;
    private String phone;
    private String gender;  // 必须是: male, female, unknown

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    private String bio;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastLoginTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 无参构造函数
    public UserProfile() {
        this.realName = "";
        this.avatar = "/default-avatar.png";
        this.phone = "";
        this.gender = "unknown";  // 使用 ENUM 允许的值
        this.bio = "这个人很懒，什么都没有写～";
    }

    // 全参构造函数
    public UserProfile(Integer id, Integer userId, String realName, String avatar,
                       String phone, String gender, Date birthday, String bio,
                       LocalDateTime lastLoginTime, LocalDateTime createdAt,
                       LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.realName = realName != null ? realName : "";
        this.avatar = avatar != null ? avatar : "/default-avatar.png";
        this.phone = phone != null ? phone : "";
        this.gender = (gender != null && (gender.equals("male") || gender.equals("female") || gender.equals("unknown")))
                ? gender : "unknown";  // 确保是有效的 ENUM 值
        this.birthday = birthday;
        this.bio = bio != null ? bio : "这个人很懒，什么都没有写～";
        this.lastLoginTime = lastLoginTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 【关键】手动添加 getter 和 setter 方法
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        // 确保设置的值是有效的 ENUM 值
        if (gender != null && (gender.equals("male") || gender.equals("female") || gender.equals("unknown"))) {
            this.gender = gender;
        } else {
            this.gender = "unknown";
        }
    }

    // 为了方便使用，添加 Integer 类型的 gender 处理方法
    public Integer getGenderInt() {
        if (gender == null) return 0;
        switch (gender) {
            case "male": return 1;
            case "female": return 2;
            default: return 0;
        }
    }

    public void setGenderInt(Integer genderInt) {
        if (genderInt == null) {
            this.gender = "unknown";
        } else if (genderInt == 1) {
            this.gender = "male";
        } else if (genderInt == 2) {
            this.gender = "female";
        } else {
            this.gender = "unknown";
        }
    }

    // 添加显示用的性别字符串（用于前端显示）
    public String getGenderDisplay() {
        if (gender == null) return "未知";
        switch (gender) {
            case "male": return "男";
            case "female": return "女";
            default: return "未知";
        }
    }

    // 设置显示用的性别字符串（从前端接收）
    public void setGenderDisplay(String genderDisplay) {
        if (genderDisplay == null) {
            this.gender = "unknown";
        } else if (genderDisplay.equals("男")) {
            this.gender = "male";
        } else if (genderDisplay.equals("女")) {
            this.gender = "female";
        } else {
            this.gender = "unknown";
        }
    }

    // 其他 getter 和 setter 方法（Lombok 应该会自动生成，但为了保险可以手动添加）
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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public LocalDateTime getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(LocalDateTime lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}