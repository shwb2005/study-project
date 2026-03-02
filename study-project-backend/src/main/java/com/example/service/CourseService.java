package com.example.service;

import com.example.entity.Course;
import com.example.entity.UserCourseRelation;

import java.util.List;

public interface CourseService {
    List<Course> getAllCourses();
    Course getCourseById(Integer id);
    List<UserCourseRelation> getUserCourses(Integer userId);
    boolean enrollCourse(Integer userId, Integer courseId);
    boolean unenrollCourse(Integer userId, Integer courseId);
    boolean rateCourse(Integer userId, Integer courseId, Integer rating, String review);
    boolean checkIn(Integer userId, Integer courseId); // 签到方法
    // 在 CourseService 接口中添加
    boolean addCourse(Course course);
    boolean deleteCourse(Integer courseId);
    boolean updateCourse(Course course);
}