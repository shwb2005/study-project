package com.example.service.course;

import com.example.entity.community.CommunityReview;
import com.example.entity.course.Course;
import com.example.entity.user.UserCourseFavorite;
import com.example.entity.user.UserCourseRelation;
import com.example.entity.study.UserStudyStats;
import com.example.mapper.community.CommunityReplyLikeMapper;
import com.example.mapper.community.CommunityReplyMapper;
import com.example.mapper.community.CommunityReviewLikeMapper;
import com.example.mapper.community.CommunityReviewMapper;
import com.example.mapper.course.CourseMapper;
import com.example.mapper.study.UserActivityLogMapper;
import com.example.mapper.user.UserCourseFavoriteMapper;
import com.example.mapper.user.UserCourseRelationMapper;
import com.example.mapper.user.UserMapper;
import com.example.mapper.user.UserProfileMapper;
import com.example.service.course.CourseService;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Resource
    private CommunityReplyMapper communityReplyMapper;

    @Resource
    private CommunityReplyLikeMapper communityReplyLikeMapper;

    @Resource
    private CommunityReviewLikeMapper communityReviewLikeMapper;

    @Resource
    private UserCourseFavoriteMapper userCourseFavoriteMapper;

    @Resource
    private UserActivityLogMapper userActivityLogMapper;

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
//        logger.info("开始查询用户 {} 的课程", userId);

        try {
            List<UserCourseRelation> relations = userCourseRelationMapper.findByUserIdWithCourse(userId);
//            logger.info("查询到 {} 条课程记录", relations.size());
            return relations;
        } catch (Exception e) {
//            logger.error("查询用户课程失败: {}", e.getMessage(), e);
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

            //更新课程人数
            Course course = courseMapper.findById(courseId);
            //更新课程人数
            course.setStudentsCount(course.getStudentsCount()+1);
            courseMapper.updateCourse(course);

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
            // 级联删除社区评价及相关数据
            try {
                Integer reviewId = communityReviewMapper.findIdByUserIdAndCourseId(userId, courseId);
                if (reviewId != null) {
                    // 删除回复的点赞
                    List<Integer> replyIds = communityReplyMapper.findIdsByReviewId(reviewId);
                    if (replyIds != null && !replyIds.isEmpty()) {
                        communityReplyLikeMapper.deleteByReplyIds(replyIds);
                    }
                    // 删除回复
                    communityReplyMapper.deleteByReviewId(reviewId);
                    // 删除评价的点赞
                    communityReviewMapper.deleteLikesByReviewId(reviewId);
                    // 删除评价
                    communityReviewMapper.deleteByUserIdAndCourseId(userId, courseId);
                    logger.info("级联删除评价成功 - userId: {}, courseId: {}, reviewId: {}", userId, courseId, reviewId);
                }
            } catch (Exception e) {
                logger.error("级联删除评价失败（不影响退课）: {}", e.getMessage(), e);
            }

            //更新课程人数
            Course course = courseMapper.findById(courseId);
            course.setStudentsCount(course.getStudentsCount()-1);
            courseMapper.updateCourse(course);

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
            return courseMapper.findWithOptions(sortBy, order, null, teacherName, page, pageSize);
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

    @Override
    public boolean toggleFavorite(Integer userId, Integer courseId) {
        try {
            UserCourseFavorite existing = userCourseFavoriteMapper.findByUserIdAndCourseId(userId, courseId);
            if (existing != null) {
                // 已收藏 → 取消
                return userCourseFavoriteMapper.deleteByUserIdAndCourseId(userId, courseId) > 0;
            } else {
                // 未收藏 → 新增
                UserCourseFavorite favorite = new UserCourseFavorite();
                favorite.setUserId(userId);
                favorite.setCourseId(courseId);
                return userCourseFavoriteMapper.insert(favorite) > 0;
            }
        } catch (Exception e) {
            logger.error("收藏操作失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public List<UserCourseFavorite> getUserFavorites(Integer userId) {
        try {
            return userCourseFavoriteMapper.findFavoritesByUserId(userId);
        } catch (Exception e) {
            logger.error("获取收藏列表失败: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public List<Integer> getFavoriteCourseIds(Integer userId) {
        try {
            return userCourseFavoriteMapper.findFavoriteCourseIdsByUserId(userId);
        } catch (Exception e) {
            logger.error("获取收藏课程ID失败: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public UserStudyStats getUserStats(Integer userId) {
        UserStudyStats stats = new UserStudyStats();
        try {
            List<UserCourseRelation> relations = userCourseRelationMapper.findByUserIdWithCourse(userId);

            int enrolledCount = relations.size();
            int completedCount = 0;
            int totalCheckIns = 0;
            double totalProgress = 0;
            int ratingCount = 0;
            double totalRating = 0;
            java.util.List<java.util.Map<String, Object>> courseProgressList = new java.util.ArrayList<>();

            for (UserCourseRelation r : relations) {
                // 完成数
                if (r.getProgress() != null && r.getProgress() >= 100) completedCount++;
                // 总签到
                totalCheckIns += (r.getCheckInCount() != null) ? r.getCheckInCount() : 0;
                // 总进度
                totalProgress += (r.getProgress() != null) ? r.getProgress() : 0;
                // 评分
                if (r.getRating() != null && r.getRating() > 0) {
                    ratingCount++;
                    totalRating += r.getRating();
                }
                // 课程进度列表（给柱状图用）
                java.util.Map<String, Object> item = new java.util.HashMap<>();
                String courseName = (r.getCourse() != null) ? r.getCourse().getName() : "课程" + r.getCourseId();
                item.put("name", courseName);
                item.put("progress", r.getProgress() != null ? r.getProgress() : 0);
                courseProgressList.add(item);
            }

            stats.setEnrolledCount(enrolledCount);
            stats.setCompletedCount(completedCount);
            stats.setTotalCheckIns(totalCheckIns);
            stats.setAvgProgress(enrolledCount > 0 ? Math.round(totalProgress / enrolledCount * 10.0) / 10.0 : 0);
            stats.setRatingCount(ratingCount);
            stats.setAvgRating(ratingCount > 0 ? Math.round(totalRating / ratingCount * 10.0) / 10.0 : 0);
            stats.setCourseProgressList(courseProgressList);

            // 收藏数
            List<Integer> favIds = userCourseFavoriteMapper.findFavoriteCourseIdsByUserId(userId);
            stats.setFavoriteCount(favIds != null ? favIds.size() : 0);

            // 最近7天签到趋势
            stats.setCheckInTrend(userActivityLogMapper.findCheckInTrend(userId));

        } catch (Exception e) {
            logger.error("获取学习统计失败: {}", e.getMessage(), e);
        }
        return stats;
    }

    @Override
    public Map<String, Object> getCoursesPaged(String search, String teacherName, String sortBy, String order, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;
            List<Course> list = courseMapper.findWithOptions(sortBy, order, search, teacherName, offset, pageSize);
            int total = courseMapper.countWithOptions(search, teacherName);
            result.put("list", list);
            result.put("total", total);
        } catch (Exception e) {
            logger.error("分页查询课程失败: {}", e.getMessage(), e);
            result.put("list", List.of());
            result.put("total", 0);
        }
        return result;
    }

    @Override
    public Map<String, Object> getUserCoursesPaged(Integer userId, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;
            List<UserCourseRelation> list = userCourseRelationMapper.findByUserIdPaged(userId, offset, pageSize);
            int total = userCourseRelationMapper.countByUserId(userId);
            result.put("list", list);
            result.put("total", total);
        } catch (Exception e) {
            logger.error("分页查询用户课程失败: {}", e.getMessage(), e);
            result.put("list", List.of());
            result.put("total", 0);
        }
        return result;
    }

    @Override
    public Map<String, Object> getUserFavoritesPaged(Integer userId, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;
            List<UserCourseFavorite> list = userCourseFavoriteMapper.findFavoritesByUserIdPaged(userId, offset, pageSize);
            int total = userCourseFavoriteMapper.countByUserId(userId);
            result.put("list", list);
            result.put("total", total);
        } catch (Exception e) {
            logger.error("分页查询收藏失败: {}", e.getMessage(), e);
            result.put("list", List.of());
            result.put("total", 0);
        }
        return result;
    }

    @Override
    public Map<String, Object> getAdminCoursesPaged(String search, int page, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        try {
            int offset = (page - 1) * pageSize;
            List<Course> list = courseMapper.findAdminPaged(search, offset, pageSize);
            int total = courseMapper.countAdmin(search);
            result.put("list", list);
            result.put("total", total);
        } catch (Exception e) {
            logger.error("管理员分页查询课程失败: {}", e.getMessage(), e);
            result.put("list", List.of());
            result.put("total", 0);
        }
        return result;
    }
}