package com.example.service;

import com.example.entity.CourseDetail;
import com.example.mapper.CourseDetailMapper;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CourseDetailService {

    private static final Logger logger = LoggerFactory.getLogger(CourseDetailService.class);

    @Resource
    private CourseDetailMapper courseDetailMapper;

    public CourseDetail getCourseDetail(Integer courseId) {
        try {
            return courseDetailMapper.findByCourseId(courseId);
        } catch (Exception e) {
            logger.error("获取课程详情失败: {}", e.getMessage(), e);
            return null;
        }
    }

    public boolean saveCourseDetail(CourseDetail detail) {
        try {
            CourseDetail existing = courseDetailMapper.findByCourseId(detail.getCourseId());
            if (existing != null) {
                return courseDetailMapper.updateByCourseId(detail) > 0;
            } else {
                return courseDetailMapper.insert(detail) > 0;
            }
        } catch (Exception e) {
            logger.error("保存课程详情失败: {}", e.getMessage(), e);
            return false;
        }
    }
}
