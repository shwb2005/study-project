package com.example.service.impl;

import com.example.entity.CommunityReview;
import com.example.entity.Course;
import com.example.entity.UserCourseRelation;
import com.example.mapper.CommunityReviewMapper;
import com.example.mapper.CourseMapper;
import com.example.mapper.UserCourseRelationMapper;
import com.example.mapper.UserMapper;
import com.example.mapper.UserProfileMapper;
import com.example.service.CourseService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseServiceImpl implements CourseService {

    private static final Logger logger = LoggerFactory.getLogger(CourseServiceImpl.class);

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private UserCourseRelationMapper userCourseRelationMapper;

    @Resource
    private CommunityReviewMapper communityReviewMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserProfileMapper userProfileMapper;

    @Override
    public List<Course> getAllCourses() {
        return courseMapper.findAll();
    }

    @Override
    public Course getCourseById(Integer id) {
        return courseMapper.findById(id);
    }

    @Override
    public List<UserCourseRelation> getUserCourses(Integer userId) {
        logger.info("开始查询用户 {} 的课程", userId);

        try {
            List<UserCourseRelation> relations = userCourseRelationMapper.findByUserIdWithCourse(userId);
            logger.info("查询到 {} 条课程记录", relations.size());
            return relations;
        } catch (Exception e) {
            logger.error("查询用户课程失败: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public boolean enrollCourse(Integer userId, Integer courseId) {
        try {
            // 检查是否已经报名
            UserCourseRelation existing = userCourseRelationMapper.findByUserIdAndCourseId(userId, courseId);
            if (existing != null) {
                return false; // 已经报名
            }

            // 获取课程信息，设置最大签到次数
            Course course = courseMapper.findById(courseId);
            Integer maxCheckInCount = (course != null && course.getMaxCheckInCount() != null)
                    ? course.getMaxCheckInCount() : 12;

            UserCourseRelation relation = new UserCourseRelation();
            relation.setUserId(userId);
            relation.setCourseId(courseId);
            relation.setProgress(0);
            relation.setCheckInCount(0); // 初始化签到次数为0
            relation.setMaxCheckInCount(maxCheckInCount); // 设置最大签到次数
            relation.setLastCheckInDate(null); // 最后签到日期为空
            relation.setIsFavorite(false);

            return userCourseRelationMapper.insert(relation) > 0;
        } catch (Exception e) {
            logger.error("报名课程失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean unenrollCourse(Integer userId, Integer courseId) {
        try {
            return userCourseRelationMapper.deleteByUserIdAndCourseId(userId, courseId) > 0;
        } catch (Exception e) {
            logger.error("取消报名失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean rateCourse(Integer userId, Integer courseId, Integer rating, String review, boolean anonymous) {
        try {
            logger.info("开始评分 - userId: {}, courseId: {}, rating: {}, anonymous: {}", userId, courseId, rating, anonymous);

            UserCourseRelation relation = userCourseRelationMapper.findByUserIdAndCourseId(userId, courseId);
            if (relation == null) {
                logger.warn("用户 {} 未报名课程 {}", userId, courseId);
                return false;
            }

            relation.setRating(rating);
            relation.setReview(review);

            int result = userCourseRelationMapper.updateRating(relation);
            boolean success = result > 0;

            if (success) {
                // 同步到社区评价表
                try {
                    Course course = courseMapper.findById(courseId);
                    String username = "匿名用户";
                    String avatar = null;
                    if (!anonymous) {
                        var account = userMapper.findWithProfileById(userId);
                        username = (account != null) ? account.getUsername() : "";
                        var profile = userProfileMapper.findByUserId(userId);
                        if (profile != null) {
                            avatar = profile.getAvatar();
                        }
                    }

                    CommunityReview cr = new CommunityReview();
                    cr.setUserId(userId);
                    cr.setUsername(username);
                    cr.setAvatar(avatar);
                    cr.setCourseId(courseId);
                    cr.setCourseName(course != null ? course.getName() : "");
                    cr.setType(0);
                    cr.setRating(rating);
                    cr.setReview(review);
                    cr.setIsAnonymous(anonymous);
                    communityReviewMapper.insertOrUpdate(cr);
                    logger.info("同步社区评价成功 - userId: {}, courseId: {}, anonymous: {}", userId, courseId, anonymous);
                } catch (Exception e) {
                    logger.error("同步社区评价失败（不影响评分）: {}", e.getMessage(), e);
                }
            }

            logger.info("评分{} - userId: {}, courseId: {}", success ? "成功" : "失败", userId, courseId);

            return success;
        } catch (Exception e) {
            logger.error("评分异常: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean checkIn(Integer userId, Integer courseId) {
        try {
            logger.info("开始签到 - userId: {}, courseId: {}", userId, courseId);

            UserCourseRelation relation = userCourseRelationMapper.findByUserIdAndCourseId(userId, courseId);
            if (relation == null) {
                logger.warn("签到失败: 用户 {} 未报名课程 {}", userId, courseId);
                return false;
            }

            // 检查今日是否已签到
            LocalDate today = LocalDate.now();
            if (relation.getLastCheckInDate() != null &&
                    relation.getLastCheckInDate().toLocalDate().equals(today)) {
                logger.warn("签到失败: 用户 {} 今日已签到课程 {}", userId, courseId);
                return false;
            }

            // 检查是否已达到最大签到次数（使用动态的最大值）
            Integer maxCheckInCount = relation.getMaxCheckInCount();
            if (maxCheckInCount == null) {
                maxCheckInCount = 12; // 默认值
            }

            if (relation.getCheckInCount() >= maxCheckInCount) {
                logger.warn("签到失败: 用户 {} 已达到最大签到次数 {}/{}",
                        userId, relation.getCheckInCount(), maxCheckInCount);
                return false;
            }

            // 更新签到信息
            int newCheckInCount = relation.getCheckInCount() + 1;
            relation.setCheckInCount(newCheckInCount);
            relation.setLastCheckInDate(LocalDateTime.now());

            // 根据签到次数更新进度
            int newProgress = Math.min(100, (newCheckInCount * 100) / maxCheckInCount);
            relation.setProgress(newProgress);

            int result = userCourseRelationMapper.updateCheckIn(relation);
            boolean success = result > 0;

            if (success) {
                logger.info("签到成功 - userId: {}, courseId: {}, 签到次数: {}/{}",
                        userId, courseId, newCheckInCount, maxCheckInCount);
            } else {
                logger.error("签到失败 - 数据库更新失败");
            }

            return success;
        } catch (Exception e) {
            logger.error("签到异常 - userId: {}, courseId: {}: {}", userId, courseId, e.getMessage(), e);
            return false;
        }
    }
    @Override
    public boolean addCourse(Course course) {
        try {
            logger.info("添加课程: {}", course.getName());
            // 这里需要调用 Mapper 插入课程
            // 假设有一个 insertCourse 方法
            return courseMapper.insertCourse(course) > 0;
        } catch (Exception e) {
            logger.error("添加课程失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean deleteCourse(Integer courseId) {
        try {
            logger.info("删除课程: {}", courseId);
            // 先删除相关的用户课程关系
            userCourseRelationMapper.deleteByCourseId(courseId);
            // 然后删除课程
            return courseMapper.deleteCourse(courseId) > 0;
        } catch (Exception e) {
            logger.error("删除课程失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean updateCourse(Course course) {
        try {
            logger.info("更新课程: {}", course.getName());
            return courseMapper.updateCourse(course) > 0;
        } catch (Exception e) {
            logger.error("更新课程失败", e);
            return false;
        }
    }
    
    @Override
    public List<Course> getCoursesSorted(String sortBy, String order) {
        try {
            return courseMapper.findAllSorted(sortBy, order);
        } catch (Exception e) {
            logger.error("排序查询失败: {}", e.getMessage(), e);
            return courseMapper.findAll();
        }
    }
    
    @Override
    public List<Course> getCoursesByTeacher(String teacherName) {
        try {
            return courseMapper.findByTeacherName(teacherName);
        } catch (Exception e) {
            logger.error("教师筛选失败: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    @Override
    public List<Course> getCoursesPaginated(int page, int pageSize) {
        try {
            return courseMapper.findPaginated(page, pageSize);
        } catch (Exception e) {
            logger.error("分页查询失败: {}", e.getMessage(), e);
            return courseMapper.findAll();
        }
    }
    
    @Override
    public List<Course> getCoursesWithOptions(String sortBy, String order, String teacherName, Integer page, Integer pageSize) {
        try {
            return courseMapper.findWithOptions(sortBy, order, teacherName, page, pageSize);
        } catch (Exception e) {
            logger.error("高级查询失败: {}", e.getMessage(), e);
            return courseMapper.findAll();
        }
    }
    
    @Override
    public List<String> getAllTeachers() {
        try {
            return courseMapper.findAllTeachers();
        } catch (Exception e) {
            logger.error("获取教师列表失败: {}", e.getMessage(), e);
            return List.of();
        }
    }
}