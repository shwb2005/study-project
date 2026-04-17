package com.example.controller.study;

import com.example.entity.RestBean;
import com.example.entity.study.StudyPlan;
import com.example.entity.study.StudyPlanItem;
import com.example.entity.user.AccountUser;
import com.example.service.study.ActivityLogService;
import com.example.service.study.StudyPlanService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/study-plan")
public class StudyPlanController {

    private static final Logger logger = LoggerFactory.getLogger(StudyPlanController.class);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private StudyPlanService studyPlanService;

    @Resource
    private ActivityLogService activityLogService;

    // ==================== 计划 CRUD ====================

    @GetMapping("/list")
    public RestBean<Map<String, Object>> getPlanList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, (Map<String, Object>) null);
            }
            Map<String, Object> data = studyPlanService.getUserPlansPaged(user.getId(), page, pageSize);
            return RestBean.success(data);
        } catch (Exception e) {
            logger.error("获取学习计划列表失败", e);
            return RestBean.failure(500, (Map<String, Object>) null);
        }
    }

    @GetMapping("/{id}")
    public RestBean<StudyPlan> getPlanById(@PathVariable Integer id, HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, (StudyPlan) null);
            }
            StudyPlan plan = studyPlanService.getPlanById(id, user.getId());
            if (plan != null) {
                return RestBean.success(plan);
            } else {
                return RestBean.failure(404, (StudyPlan) null);
            }
        } catch (Exception e) {
            logger.error("获取学习计划详情失败", e);
            return RestBean.failure(500, (StudyPlan) null);
        }
    }

    @PostMapping("/create")
    public RestBean<String> createPlan(@RequestParam String title,
                                       @RequestParam String startDate,
                                       @RequestParam String endDate,
                                       @RequestParam(required = false) String description,
                                       HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }
            StudyPlan plan = new StudyPlan();
            plan.setUserId(user.getId());
            plan.setTitle(title);
            plan.setDescription(description);
            plan.setStartDate(LocalDate.parse(startDate, DATE_FMT));
            plan.setEndDate(LocalDate.parse(endDate, DATE_FMT));
            plan.setStatus(0);

            if (studyPlanService.createPlan(plan)) {
                activityLogService.log(user.getId(), "创建学习计划", "创建了学习计划「" + title + "」");
                return RestBean.success("创建成功");
            } else {
                return RestBean.failure(400, "创建失败");
            }
        } catch (Exception e) {
            logger.error("创建学习计划失败", e);
            return RestBean.failure(500, "创建失败");
        }
    }

    @PostMapping("/update")
    public RestBean<String> updatePlan(@RequestParam Integer id,
                                       @RequestParam String title,
                                       @RequestParam String startDate,
                                       @RequestParam String endDate,
                                       @RequestParam(defaultValue = "0") Integer status,
                                       @RequestParam(required = false) String description,
                                       HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }
            StudyPlan plan = new StudyPlan();
            plan.setId(id);
            plan.setUserId(user.getId());
            plan.setTitle(title);
            plan.setDescription(description);
            plan.setStartDate(LocalDate.parse(startDate, DATE_FMT));
            plan.setEndDate(LocalDate.parse(endDate, DATE_FMT));
            plan.setStatus(status);

            if (studyPlanService.updatePlan(plan)) {
                activityLogService.log(user.getId(), "更新学习计划", "更新了学习计划「" + title + "」");
                return RestBean.success("更新成功");
            } else {
                return RestBean.failure(400, "更新失败");
            }
        } catch (Exception e) {
            logger.error("更新学习计划失败", e);
            return RestBean.failure(500, "更新失败");
        }
    }

    @PostMapping("/delete")
    public RestBean<String> deletePlan(@RequestParam Integer id, HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }
            if (studyPlanService.deletePlan(id, user.getId())) {
                activityLogService.log(user.getId(), "删除学习计划", "删除了学习计划");
                return RestBean.success("删除成功");
            } else {
                return RestBean.failure(400, "删除失败");
            }
        } catch (Exception e) {
            logger.error("删除学习计划失败", e);
            return RestBean.failure(500, "删除失败");
        }
    }

    // ==================== 日历视图 ====================

    @GetMapping("/calendar")
    public RestBean<List<StudyPlanItem>> getCalendarItems(
            @RequestParam String startDate,
            @RequestParam String endDate,
            HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.success(Collections.emptyList());
            }
            LocalDate start = LocalDate.parse(startDate, DATE_FMT);
            LocalDate end = LocalDate.parse(endDate, DATE_FMT);
            List<StudyPlanItem> items = studyPlanService.getCalendarItems(user.getId(), start, end);
            return RestBean.success(items);
        } catch (Exception e) {
            logger.error("获取日历数据失败", e);
            return RestBean.failure(500, Collections.emptyList());
        }
    }

    // ==================== 任务项 CRUD ====================

    @PostMapping("/item/add")
    public RestBean<String> addItem(
            @RequestParam Integer planId,
            @RequestParam String planDate,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String customTopic,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String notes,
            HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }
            StudyPlanItem item = new StudyPlanItem();
            item.setPlanId(planId);
            item.setCourseId(courseId);
            item.setCustomTopic(customTopic);
            item.setPlanDate(LocalDate.parse(planDate, DATE_FMT));
            item.setStartTime(startTime);
            item.setEndTime(endTime);
            item.setNotes(notes);
            item.setCompleted(false);
            item.setSortOrder(0);

            if (studyPlanService.addPlanItem(item, user.getId())) {
                String topic = courseId != null ? "课程学习" : (customTopic != null ? customTopic : "学习任务");
                activityLogService.log(user.getId(), "添加学习任务", "添加了学习任务「" + topic + "」");
                return RestBean.success("添加成功");
            } else {
                return RestBean.failure(400, "添加失败");
            }
        } catch (Exception e) {
            logger.error("添加计划任务失败", e);
            return RestBean.failure(500, "添加失败");
        }
    }

    @PostMapping("/item/add-batch")
    public RestBean<Map<String, Object>> addItemBatch(
            @RequestParam Integer planId,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String customTopic,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String notes,
            @RequestParam List<Integer> weekdays,
            HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, (Map<String, Object>) null);
            }

            if (weekdays == null || weekdays.isEmpty()) {
                return RestBean.failure(400, Collections.singletonMap("success", false));
            }

            Map<String, Object> result = studyPlanService.addPlanItemsBatch(
                    planId, courseId, customTopic, startTime, endTime, notes,
                    weekdays, user.getId()
            );

            if ((Boolean) result.get("success")) {
                String topic = courseId != null ? "课程学习" : (customTopic != null ? customTopic : "学习任务");
                int createdCount = (Integer) result.get("createdCount");
                activityLogService.log(user.getId(), "批量添加学习任务",
                        "添加了「" + topic + "」的重复任务，共创建" + createdCount + "个");
                return RestBean.success(result);
            } else {
                return RestBean.failure(400, result);
            }
        } catch (Exception e) {
            logger.error("批量添加计划任务失败", e);
            return RestBean.failure(500, Collections.singletonMap("success", false));
        }
    }

    @PostMapping("/item/update")
    public RestBean<String> updateItem(
            @RequestParam Integer id,
            @RequestParam Integer planId,
            @RequestParam String planDate,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String customTopic,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime,
            @RequestParam(required = false) String notes,
            HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }
            StudyPlanItem item = new StudyPlanItem();
            item.setId(id);
            item.setPlanId(planId);
            item.setCourseId(courseId);
            item.setCustomTopic(customTopic);
            item.setPlanDate(LocalDate.parse(planDate, DATE_FMT));
            item.setStartTime(startTime);
            item.setEndTime(endTime);
            item.setNotes(notes);
            item.setCompleted(false);
            item.setSortOrder(0);

            if (studyPlanService.updatePlanItem(item, user.getId())) {
                return RestBean.success("更新成功");
            } else {
                return RestBean.failure(400, "更新失败");
            }
        } catch (Exception e) {
            logger.error("更新计划任务失败", e);
            return RestBean.failure(500, "更新失败");
        }
    }

    @PostMapping("/item/delete")
    public RestBean<String> deleteItem(@RequestParam Integer id,
                                       @RequestParam Integer planId,
                                       HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }
            if (studyPlanService.deletePlanItem(id, planId, user.getId())) {
                return RestBean.success("删除成功");
            } else {
                return RestBean.failure(400, "删除失败");
            }
        } catch (Exception e) {
            logger.error("删除计划任务失败", e);
            return RestBean.failure(500, "删除失败");
        }
    }

    @PostMapping("/item/toggle")
    public RestBean<String> toggleItem(@RequestParam Integer id,
                                       @RequestParam boolean completed,
                                       HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }
            if (studyPlanService.toggleItemCompletion(id, completed)) {
                activityLogService.log(user.getId(),
                        completed ? "完成学习任务" : "取消完成任务",
                        completed ? "完成了一项学习任务" : "取消完成了一项学习任务");
                return RestBean.success("操作成功");
            } else {
                return RestBean.failure(400, "操作失败");
            }
        } catch (Exception e) {
            logger.error("切换任务状态失败", e);
            return RestBean.failure(500, "操作失败");
        }
    }
}
