package com.example.contorller;

import com.example.entity.RestBean;
import com.example.entity.Course;
import com.example.entity.CourseDetail;
import com.example.entity.user.Admin;
import com.example.service.CourseService;
import com.example.service.CourseDetailService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")  // 管理员专用路径
public class AdminCourseController {

    private static final Logger logger = LoggerFactory.getLogger(AdminCourseController.class);

    @Resource
    private CourseService courseService;

    @Resource
    private CourseDetailService courseDetailService;

    @GetMapping("/course/list")
    public RestBean<Map<String, Object>> getAdminCourseList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "8") int pageSize,
            @RequestParam(required = false) String search,
            HttpSession session) {
        try {
            Admin admin = (Admin) session.getAttribute("admin");
            if (admin == null) {
                logger.warn("无权限操作: session 中未找到管理员信息");
                return RestBean.failure(401, (Map<String, Object>) null);
            }

            Map<String, Object> data = courseService.getAdminCoursesPaged(search, page, pageSize);
            return RestBean.success(data);
        } catch (Exception e) {
            logger.error("获取课程列表失败", e);
            return RestBean.failure(500, (Map<String, Object>) null);
        }
    }

    @GetMapping("/course/detail")
    public RestBean<CourseDetail> getCourseDetail(@RequestParam Integer courseId, HttpSession session) {
        try {
            Admin admin = (Admin) session.getAttribute("admin");
            if (admin == null) {
                return RestBean.failure(401, (CourseDetail) null);
            }
            CourseDetail detail = courseDetailService.getCourseDetail(courseId);
            return RestBean.success(detail);
        } catch (Exception e) {
            return RestBean.success(null);
        }
    }

    @PostMapping("/course/add")
    public RestBean<String> addAdminCourse(@RequestParam String name,
                                           @RequestParam String description,
                                           @RequestParam String teacherName,
                                           @RequestParam String duration,
                                           @RequestParam(defaultValue = "12") Integer maxCheckInCount,
                                           @RequestParam(required = false) String objectives,
                                           @RequestParam(required = false) String outline,
                                           @RequestParam(required = false) String requirements,
                                           @RequestParam(required = false) String audience,
                                           @RequestParam(required = false) String materials,
                                           @RequestParam(required = false) String coverImage,
                                           @RequestParam(required = false) String videoUrl,
                                           HttpSession session) {
        try {
            Admin admin = (Admin) session.getAttribute("admin");
            if (admin == null) {
                return RestBean.failure(401, "请先登录管理员账户");
            }

            Course course = new Course();
            course.setName(name);
            course.setDescription(description);
            course.setTeacherName(teacherName);
            course.setDuration(duration);
            course.setStudentsCount(0);
            course.setRating(0.0);
            course.setStatus("published");
            course.setMaxCheckInCount(maxCheckInCount);
            course.setCoverImage(coverImage);
            course.setVideoUrl(videoUrl);

            boolean success = courseService.addCourse(course);
            if (success) {
                // 保存课程详情
                try {
                    CourseDetail detail = new CourseDetail();
                    detail.setCourseId(course.getId());
                    detail.setObjectives(objectives);
                    detail.setOutline(outline);
                    detail.setRequirements(requirements);
                    detail.setAudience(audience);
                    detail.setMaterials(materials);
                    courseDetailService.saveCourseDetail(detail);
                } catch (Exception e) {
                    logger.warn("保存课程详情失败（不影响课程创建）: {}", e.getMessage());
                }
                return RestBean.success("课程添加成功");
            } else {
                logger.error("课程添加失败: {}", name);
                return RestBean.failure(500, "课程添加失败");
            }
        } catch (Exception e) {
            logger.error("添加课程失败", e);
            return RestBean.failure(500, "添加课程失败: " + e.getMessage());
        }
    }

    @PostMapping("/course/delete")
    public RestBean<String> deleteAdminCourse(@RequestParam Integer courseId, HttpSession session) {
        try {

            // 检查管理员权限
            Admin admin = (Admin) session.getAttribute("admin");
            if (admin == null) {
                return RestBean.failure(401, "请先登录管理员账户");
            }

            boolean success = courseService.deleteCourse(courseId);
            if (success) {
                return RestBean.success("课程删除成功");
            } else {
                logger.error("课程删除失败: {}", courseId);
                return RestBean.failure(500, "课程删除失败");
            }
        } catch (Exception e) {
            logger.error("删除课程失败", e);
            return RestBean.failure(500, "删除课程失败: " + e.getMessage());
        }
    }
    @PostMapping("/course/update")
    public RestBean<String> updateAdminCourse(@RequestParam Integer id,
                                              @RequestParam String name,
                                              @RequestParam String description,
                                              @RequestParam String teacherName,
                                              @RequestParam String duration,
                                              @RequestParam Integer studentsCount,
                                              @RequestParam Integer maxCheckInCount,
                                              @RequestParam(required = false) String objectives,
                                              @RequestParam(required = false) String outline,
                                              @RequestParam(required = false) String requirements,
                                              @RequestParam(required = false) String audience,
                                              @RequestParam(required = false) String materials,
                                              @RequestParam(required = false) String coverImage,
                                              @RequestParam(required = false) String videoUrl,
                                              HttpSession session) {
        try {
            Admin admin = (Admin) session.getAttribute("admin");
            if (admin == null) {
                return RestBean.failure(401, "请先登录管理员账户");
            }

            Course course = new Course();
            course.setId(id);
            course.setName(name);
            course.setDescription(description);
            course.setTeacherName(teacherName);
            course.setDuration(duration);
            course.setStudentsCount(studentsCount);
            course.setMaxCheckInCount(maxCheckInCount);
            course.setCoverImage(coverImage);
            course.setVideoUrl(videoUrl);

            boolean success = courseService.updateCourse(course);
            if (success) {
                // 保存课程详情
                try {
                    CourseDetail detail = new CourseDetail();
                    detail.setCourseId(id);
                    detail.setObjectives(objectives);
                    detail.setOutline(outline);
                    detail.setRequirements(requirements);
                    detail.setAudience(audience);
                    detail.setMaterials(materials);
                    courseDetailService.saveCourseDetail(detail);
                } catch (Exception e) {
                    logger.warn("保存课程详情失败（不影响课程更新）: {}", e.getMessage());
                }
                return RestBean.success("课程更新成功");
            } else {
                return RestBean.failure(500, "课程更新失败");
            }
        } catch (Exception e) {
            logger.error("更新课程失败", e);
            return RestBean.failure(500, "更新课程失败: " + e.getMessage());
        }
    }
}