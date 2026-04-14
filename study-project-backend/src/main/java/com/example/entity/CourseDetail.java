package com.example.entity;

import java.time.LocalDateTime;

public class CourseDetail {
    private Integer id;
    private Integer courseId;
    private String objectives;
    private String outline;
    private String requirements;
    private String audience;
    private String materials;
    private LocalDateTime updatedAt;

    public CourseDetail() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getCourseId() { return courseId; }
    public void setCourseId(Integer courseId) { this.courseId = courseId; }
    public String getObjectives() { return objectives; }
    public void setObjectives(String objectives) { this.objectives = objectives; }
    public String getOutline() { return outline; }
    public void setOutline(String outline) { this.outline = outline; }
    public String getRequirements() { return requirements; }
    public void setRequirements(String requirements) { this.requirements = requirements; }
    public String getAudience() { return audience; }
    public void setAudience(String audience) { this.audience = audience; }
    public String getMaterials() { return materials; }
    public void setMaterials(String materials) { this.materials = materials; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
