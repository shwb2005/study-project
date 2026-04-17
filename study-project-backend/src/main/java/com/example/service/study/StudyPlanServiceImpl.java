package com.example.service.study;

import com.example.entity.study.StudyPlan;
import com.example.entity.study.StudyPlanItem;
import com.example.mapper.study.StudyPlanMapper;
import com.example.service.study.StudyPlanService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class StudyPlanServiceImpl implements StudyPlanService {

    private static final Logger logger = LoggerFactory.getLogger(StudyPlanServiceImpl.class);

    @Resource
    private StudyPlanMapper studyPlanMapper;

    @Override
    public List<StudyPlan> getUserPlans(Integer userId) {
        List<StudyPlan> plans = studyPlanMapper.findByUserId(userId);
        for (StudyPlan plan : plans) {
            fillPlanStats(plan);
        }
        return plans;
    }

    @Override
    public StudyPlan getPlanById(Integer planId, Integer userId) {
        StudyPlan plan = studyPlanMapper.findByIdAndUserId(planId, userId);
        if (plan != null) {
            List<StudyPlanItem> items = studyPlanMapper.findItemsByPlanId(planId);
            plan.setItems(items);
            fillPlanStats(plan);
        }
        return plan;
    }

    @Override
    public List<StudyPlanItem> getCalendarItems(Integer userId, LocalDate startDate, LocalDate endDate) {
        return studyPlanMapper.findItemsByUserIdAndDateRange(userId, startDate, endDate);
    }

    @Override
    public boolean createPlan(StudyPlan plan) {
        try {
            plan.setStatus(0);
            return studyPlanMapper.insert(plan) > 0;
        } catch (Exception e) {
            logger.error("创建学习计划失败", e);
            return false;
        }
    }

    @Override
    public boolean updatePlan(StudyPlan plan) {
        try {
            return studyPlanMapper.update(plan) > 0;
        } catch (Exception e) {
            logger.error("更新学习计划失败", e);
            return false;
        }
    }

    @Override
    public boolean deletePlan(Integer planId, Integer userId) {
        try {
            return studyPlanMapper.deleteByIdAndUserId(planId, userId) > 0;
        } catch (Exception e) {
            logger.error("删除学习计划失败", e);
            return false;
        }
    }

    @Override
    public boolean addPlanItem(StudyPlanItem item, Integer userId) {
        try {
            // 验证计划属于该用户
            StudyPlan plan = studyPlanMapper.findByIdAndUserId(item.getPlanId(), userId);
            if (plan == null) {
                return false;
            }
            if (item.getCompleted() == null) {
                item.setCompleted(false);
            }
            if (item.getSortOrder() == null) {
                item.setSortOrder(0);
            }
            return studyPlanMapper.insertItem(item) > 0;
        } catch (Exception e) {
            logger.error("添加计划任务失败", e);
            return false;
        }
    }

    @Override
    public boolean updatePlanItem(StudyPlanItem item, Integer userId) {
        try {
            // 验证计划属于该用户
            StudyPlan plan = studyPlanMapper.findByIdAndUserId(item.getPlanId(), userId);
            if (plan == null) {
                return false;
            }
            return studyPlanMapper.updateItem(item) > 0;
        } catch (Exception e) {
            logger.error("更新计划任务失败", e);
            return false;
        }
    }

    @Override
    public boolean deletePlanItem(Integer itemId, Integer planId, Integer userId) {
        try {
            // 验证计划属于该用户
            StudyPlan plan = studyPlanMapper.findByIdAndUserId(planId, userId);
            if (plan == null) {
                return false;
            }
            return studyPlanMapper.deleteItem(itemId, planId) > 0;
        } catch (Exception e) {
            logger.error("删除计划任务失败", e);
            return false;
        }
    }

    @Override
    public boolean toggleItemCompletion(Integer itemId, boolean completed) {
        try {
            return studyPlanMapper.updateItemCompletion(itemId, completed) > 0;
        } catch (Exception e) {
            logger.error("切换任务完成状态失败", e);
            return false;
        }
    }

    @Override
    public Map<String, Object> getUserPlansPaged(Integer userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        List<StudyPlan> plans = studyPlanMapper.findByUserIdPaged(userId, offset, pageSize);
        int total = studyPlanMapper.countByUserId(userId);

        for (StudyPlan plan : plans) {
            fillPlanStats(plan);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("list", plans);
        data.put("total", total);
        data.put("page", page);
        data.put("pageSize", pageSize);
        data.put("totalPages", (int) Math.ceil((double) total / pageSize));
        return data;
    }

    private void fillPlanStats(StudyPlan plan) {
        List<StudyPlanItem> items = studyPlanMapper.findItemsByPlanId(plan.getId());
        plan.setItemCount(items.size());
        plan.setCompletedCount((int) items.stream().filter(i -> Boolean.TRUE.equals(i.getCompleted())).count());
    }

    @Override
    public Map<String, Object> addPlanItemsBatch(Integer planId, Integer courseId, String customTopic,
                                                  String startTime, String endTime, String notes,
                                                  List<Integer> weekdays, Integer userId) {
        Map<String, Object> result = new HashMap<>();

        try {
            logger.info("=== 批量添加学习任务开始 ===");
            logger.info("用户ID: {}", userId);
            logger.info("计划ID: {}", planId);
            logger.info("课程ID: {}", courseId);
            logger.info("自定义主题: {}", customTopic);
            logger.info("开始时间: {}", startTime);
            logger.info("结束时间: {}", endTime);
            logger.info("备注: {}", notes);
            logger.info("选择的星期: {}", weekdays);

            // 1. 验证计划属于该用户
            StudyPlan plan = studyPlanMapper.findByIdAndUserId(planId, userId);
            if (plan == null) {
                logger.warn("计划不存在或无权限，计划ID: {}, 用户ID: {}", planId, userId);
                result.put("success", false);
                result.put("message", "计划不存在或无权限");
                return result;
            }

            // 2. 获取计划的开始和结束日期
            LocalDate startDate = plan.getStartDate();
            LocalDate endDate = plan.getEndDate();
            logger.info("计划周期: {} 至 {}", startDate, endDate);

            // 3. 计算周期内所有符合条件的日期
            List<LocalDate> targetDates = calculateTargetDates(startDate, endDate, weekdays);
            logger.info("计算出的目标日期数量: {}", targetDates.size());
            logger.info("目标日期列表: {}", targetDates);

            if (targetDates.isEmpty()) {
                logger.warn("所选日期范围内没有符合条件的星期");
                result.put("success", false);
                result.put("message", "所选日期范围内没有符合条件的星期");
                return result;
            }

            // 4. 批量插入任务记录
            int createdCount = 0;
            int sortOrder = 0;

            for (LocalDate targetDate : targetDates) {
                StudyPlanItem item = new StudyPlanItem();
                item.setPlanId(planId);
                item.setCourseId(courseId);
                item.setCustomTopic(customTopic);
                item.setPlanDate(targetDate);
                item.setStartTime(startTime);
                item.setEndTime(endTime);
                item.setNotes(notes);
                item.setCompleted(false);
                item.setSortOrder(sortOrder++);

                if (studyPlanMapper.insertItem(item) > 0) {
                    createdCount++;
                    logger.debug("成功创建任务，日期: {}", targetDate);
                } else {
                    logger.warn("创建任务失败，日期: {}", targetDate);
                }
            }

            logger.info("批量添加完成，成功创建 {} 个任务", createdCount);
            result.put("success", true);
            result.put("message", "批量添加成功");
            result.put("createdCount", createdCount);

            return result;

        } catch (Exception e) {
            logger.error("批量添加计划任务失败", e);
            result.put("success", false);
            result.put("message", "添加失败：" + e.getMessage());
            return result;
        }
    }

    /**
     * 计算指定日期范围内所有符合星期几的日期
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param weekdays 星期几列表 (1=周一, 7=周日)
     * @return 符合条件的日期列表
     */
    private List<LocalDate> calculateTargetDates(LocalDate startDate, LocalDate endDate, List<Integer> weekdays) {
        List<LocalDate> result = new ArrayList<>();

        // 将星期几转换为Set以提高查询效率
        Set<Integer> weekdaySet = new HashSet<>(weekdays);

        // 遍历日期范围
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            // 获取当前日期的星期几 (1=周一, 7=周日)
            int dayOfWeek = currentDate.getDayOfWeek().getValue();

            // 如果当前星期几在用户选择的列表中，则添加到结果
            if (weekdaySet.contains(dayOfWeek)) {
                result.add(currentDate);
            }

            // 移动到下一天
            currentDate = currentDate.plusDays(1);
        }

        return result;
    }
}
