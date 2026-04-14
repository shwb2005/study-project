package com.example.service;

import com.example.entity.Course;
import com.example.entity.UserCourseFavorite;
import com.example.entity.UserCourseRelation;
import com.example.entity.UserStudyStats;

import java.util.List;
import java.util.Map;

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

    // 收藏相关
    boolean toggleFavorite(Integer userId, Integer courseId);
    List<UserCourseFavorite> getUserFavorites(Integer userId);
    List<Integer> getFavoriteCourseIds(Integer userId);
    UserStudyStats getUserStats(Integer userId);

    // 分页
    Map<String, Object> getCoursesPaged(String search, String teacherName, String sortBy, String order, int page, int pageSize);
    Map<String, Object> getUserCoursesPaged(Integer userId, int page, int pageSize);
    Map<String, Object> getUserFavoritesPaged(Integer userId, int page, int pageSize);
    Map<String, Object> getAdminCoursesPaged(String search, int page, int pageSize);
}