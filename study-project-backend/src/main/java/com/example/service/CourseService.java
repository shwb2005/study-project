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
    boolean rateCourse(Integer userId, Integer courseId, Integer rating, String review, boolean anonymous);
    boolean checkIn(Integer userId, Integer courseId);
    boolean addCourse(Course course);
    boolean deleteCourse(Integer courseId);
    boolean updateCourse(Course course);
    
    List<Course> getCoursesSorted(String sortBy, String order);
    List<Course> getCoursesByTeacher(String teacherName);
    List<Course> getCoursesPaginated(int page, int pageSize);
    List<Course> getCoursesWithOptions(String sortBy, String order, String teacherName, Integer page, Integer pageSize);
    List<String> getAllTeachers();
}