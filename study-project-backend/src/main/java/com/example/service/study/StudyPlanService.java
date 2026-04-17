package com.example.service.study;

import com.example.entity.study.StudyPlan;
import com.example.entity.study.StudyPlanItem;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface StudyPlanService {
    List<StudyPlan> getUserPlans(Integer userId);
    StudyPlan getPlanById(Integer planId, Integer userId);
    List<StudyPlanItem> getCalendarItems(Integer userId, LocalDate startDate, LocalDate endDate);
    boolean createPlan(StudyPlan plan);
    boolean updatePlan(StudyPlan plan);
    boolean deletePlan(Integer planId, Integer userId);
    boolean addPlanItem(StudyPlanItem item, Integer userId);
    boolean updatePlanItem(StudyPlanItem item, Integer userId);
    boolean deletePlanItem(Integer itemId, Integer planId, Integer userId);
    boolean toggleItemCompletion(Integer itemId, boolean completed);
    Map<String, Object> getUserPlansPaged(Integer userId, int page, int pageSize);
    Map<String, Object> addPlanItemsBatch(Integer planId, Integer courseId, String customTopic,
                                          String startTime, String endTime, String notes,
                                          List<Integer> weekdays, Integer userId);
}
