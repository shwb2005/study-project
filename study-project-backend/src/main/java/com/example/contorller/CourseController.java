package com.example.contorller;

import com.example.entity.RestBean;
import com.example.entity.Course;
import com.example.entity.UserCourseRelation;
import com.example.entity.user.AccountUser;
import com.example.entity.user.Admin;
import com.example.service.CourseService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Collections;

@RestController
@RequestMapping("/api/course")
public class CourseController {

    private static final Logger logger = LoggerFactory.getLogger(CourseController.class);

    @Resource
    private CourseService courseService;

    @GetMapping("/list")
    public RestBean<List<Course>> getAllCourses() {
        try {
            List<Course> courses = courseService.getAllCourses();
            return RestBean.success(courses);
        } catch (Exception e) {
            return RestBean.failure(500, Collections.emptyList());
        }
    }

    @GetMapping("/{id}")
    public RestBean<Course> getCourseById(@PathVariable Integer id) {
        try {
            Course course = courseService.getCourseById(id);
            if (course != null) {
                return RestBean.success(course);
            } else {
                return RestBean.failure(404, (Course) null);
            }
        } catch (Exception e) {
            return RestBean.failure(500, (Course) null);
        }
    }

    @GetMapping("/my-courses")
    public RestBean<List<UserCourseRelation>> getMyCourses(HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, Collections.emptyList());
            }

            List<UserCourseRelation> courses = courseService.getUserCourses(user.getId());
            return RestBean.success(courses);
        } catch (Exception e) {
            return RestBean.failure(500, Collections.emptyList());
        }
    }

    @PostMapping("/enroll")
    public RestBean<String> enrollCourse(@RequestParam Integer courseId, HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }

            if (courseService.enrollCourse(user.getId(), courseId)) {
                return RestBean.success("报名成功");
            } else {
                return RestBean.failure(400, "报名失败，可能已经报名该课程");
            }
        } catch (Exception e) {
            return RestBean.failure(500, "报名失败");
        }
    }

    @PostMapping("/unenroll")
    public RestBean<String> unenrollCourse(@RequestParam Integer courseId, HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }

            if (courseService.unenrollCourse(user.getId(), courseId)) {
                return RestBean.success("取消报名成功");
            } else {
                return RestBean.failure(400, "取消报名失败");
            }
        } catch (Exception e) {
            return RestBean.failure(500, "取消报名失败");
        }
    }

    @PostMapping("/rate")
    public RestBean<String> rateCourse(@RequestParam Integer courseId,
                                       @RequestParam Integer rating,
                                       @RequestParam(required = false) String review,
                                       HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }

            if (courseService.rateCourse(user.getId(), courseId, rating, review)) {
                return RestBean.success("评分成功");
            } else {
                return RestBean.failure(400, "评分失败");
            }
        } catch (Exception e) {
            return RestBean.failure(500, "评分失败");
        }
    }

    @PostMapping("/check-in")
    public RestBean<String> checkIn(@RequestParam Integer courseId, HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }

            if (courseService.checkIn(user.getId(), courseId)) {
                return RestBean.success("签到成功");
            } else {
                return RestBean.failure(400, "签到失败，今日已签到或已达到最大签到次数");
            }
        } catch (Exception e) {
            return RestBean.failure(500, "签到失败");
        }
    }

}