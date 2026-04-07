package com.example.contorller;

import com.example.entity.RestBean;
import com.example.entity.Course;
import com.example.entity.CommunityReply;
import com.example.entity.CommunityReview;
import com.example.entity.CommunityReviewLike;
import com.example.entity.CommunityReplyLike;
import com.example.entity.UserCourseRelation;
import com.example.entity.user.AccountUser;
import com.example.entity.user.Admin;
import com.example.mapper.CommunityReplyMapper;
import com.example.mapper.CommunityReplyLikeMapper;
import com.example.mapper.CommunityReviewLikeMapper;
import com.example.mapper.CommunityReviewMapper;
import com.example.mapper.UserMapper;
import com.example.mapper.UserProfileMapper;
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

    @Resource
    private CommunityReviewMapper communityReviewMapper;

    @Resource
    private CommunityReviewLikeMapper communityReviewLikeMapper;

    @Resource
    private CommunityReplyMapper communityReplyMapper;

    @Resource
    private CommunityReplyLikeMapper communityReplyLikeMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserProfileMapper userProfileMapper;

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
                                       @RequestParam(defaultValue = "false") boolean anonymous,
                                       HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }

            if (courseService.rateCourse(user.getId(), courseId, rating, review, anonymous)) {
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

    @GetMapping("/community")
    public RestBean<List<CommunityReview>> getCommunityReviews() {
        try {
            List<CommunityReview> reviews = communityReviewMapper.findAll();
            logger.info("社区评价查询结果: {} 条记录", reviews.size());
            for (CommunityReview r : reviews) {
                logger.info("评价详情: id={}, user={}, course={}, rating={}, review={}",
                        r.getId(), r.getUsername(), r.getCourseName(), r.getRating(), r.getReview());
            }
            return RestBean.success(reviews);
        } catch (Exception e) {
            logger.error("获取社区评价失败", e);
            return RestBean.failure(500, Collections.emptyList());
        }
    }

    // ==================== 社区互动 API ====================

    @PostMapping("/community/like")
    public RestBean<String> toggleLike(@RequestParam Integer reviewId,
                                       @RequestParam Integer type,
                                       HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }

            CommunityReviewLike existing = communityReviewLikeMapper.findByUserAndReview(user.getId(), reviewId);

            if (existing != null) {
                if (existing.getType().equals(type)) {
                    // 取消操作
                    communityReviewLikeMapper.deleteByUserAndReview(user.getId(), reviewId);
                    if (type == 1) {
                        communityReviewMapper.updateLikeCount(reviewId, -1);
                    } else {
                        communityReviewMapper.updateDislikeCount(reviewId, -1);
                    }
                    return RestBean.success("已取消");
                } else {
                    // 切换类型：先减旧计数，再加新计数
                    if (existing.getType() == 1) {
                        communityReviewMapper.updateLikeCount(reviewId, -1);
                        communityReviewMapper.updateDislikeCount(reviewId, 1);
                    } else {
                        communityReviewMapper.updateDislikeCount(reviewId, -1);
                        communityReviewMapper.updateLikeCount(reviewId, 1);
                    }
                    existing.setType(type);
                    communityReviewLikeMapper.insertOrUpdate(existing);
                    return RestBean.success(type == 1 ? "已点赞" : "已点踩");
                }
            } else {
                // 新操作
                CommunityReviewLike like = new CommunityReviewLike();
                like.setUserId(user.getId());
                like.setReviewId(reviewId);
                like.setType(type);
                communityReviewLikeMapper.insertOrUpdate(like);
                if (type == 1) {
                    communityReviewMapper.updateLikeCount(reviewId, 1);
                } else {
                    communityReviewMapper.updateDislikeCount(reviewId, 1);
                }
                return RestBean.success(type == 1 ? "已点赞" : "已点踩");
            }
        } catch (Exception e) {
            logger.error("点赞/点踩失败", e);
            return RestBean.failure(500, "操作失败");
        }
    }

    @GetMapping("/community/like-status")
    public RestBean<CommunityReviewLike> getLikeStatus(@RequestParam Integer reviewId,
                                                       HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.success(null);
            }
            CommunityReviewLike like = communityReviewLikeMapper.findByUserAndReview(user.getId(), reviewId);
            return RestBean.success(like);
        } catch (Exception e) {
            return RestBean.success(null);
        }
    }

    @GetMapping("/community/like-statuses")
    public RestBean<List<CommunityReviewLike>> getLikeStatuses(@RequestParam List<Integer> reviewIds,
                                                               HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.success(Collections.emptyList());
            }
            List<CommunityReviewLike> likes = communityReviewLikeMapper.findByUserAndReviewIds(user.getId(), reviewIds);
            return RestBean.success(likes);
        } catch (Exception e) {
            return RestBean.success(Collections.emptyList());
        }
    }

    @PostMapping("/community/reply")
    public RestBean<String> replyReview(@RequestParam Integer reviewId,
                                        @RequestParam String content,
                                        @RequestParam(defaultValue = "false") boolean anonymous,
                                        @RequestParam(required = false) Integer parentReplyId,
                                        HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }

            String username = "匿名用户";
            String avatar = null;
            if (!anonymous) {
                var account = userMapper.findWithProfileById(user.getId());
                username = (account != null) ? account.getUsername() : "";
                var profile = userProfileMapper.findByUserId(user.getId());
                if (profile != null) {
                    avatar = profile.getAvatar();
                }
            }

            CommunityReply reply = new CommunityReply();
            reply.setReviewId(reviewId);
            reply.setUserId(user.getId());
            reply.setUsername(username);
            reply.setAvatar(avatar);
            reply.setContent(content);
            reply.setIsAnonymous(anonymous);

            // 如果是二级回复
            if (parentReplyId != null) {
                reply.setParentReplyId(parentReplyId);
                // 获取被回复的回复信息
                List<CommunityReply> allReplies = communityReplyMapper.findByReviewId(reviewId, user.getId());
                CommunityReply parentReply = allReplies.stream()
                        .filter(r -> r.getId().equals(parentReplyId))
                        .findFirst()
                        .orElse(null);
                if (parentReply != null) {
                    reply.setReplyToUserId(parentReply.getUserId());
                    reply.setReplyToUsername(parentReply.getIsAnonymous() ? "匿名用户" : parentReply.getUsername());
                }
            }

            communityReplyMapper.insert(reply);

            return RestBean.success("回复成功");
        } catch (Exception e) {
            logger.error("回复评价失败", e);
            return RestBean.failure(500, "回复失败");
        }
    }

    @GetMapping("/community/replies")
    public RestBean<List<CommunityReply>> getReplies(@RequestParam Integer reviewId,
                                                      HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            Integer currentUserId = (user != null) ? user.getId() : null;
            List<CommunityReply> replies = communityReplyMapper.findByReviewId(reviewId, currentUserId);
            return RestBean.success(replies);
        } catch (Exception e) {
            logger.error("获取回复失败", e);
            return RestBean.failure(500, Collections.emptyList());
        }
    }

    @PostMapping("/community/reply/like")
    public RestBean<String> toggleReplyLike(@RequestParam Integer replyId,
                                            @RequestParam Integer type,
                                            HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }

            CommunityReplyLike existing = communityReplyLikeMapper.findByUserAndReply(user.getId(), replyId);

            if (existing != null) {
                if (existing.getType().equals(type)) {
                    // 取消操作
                    communityReplyLikeMapper.deleteByUserAndReply(user.getId(), replyId);
                    if (type == 1) {
                        communityReplyMapper.updateLikeCount(replyId, -1);
                    } else {
                        communityReplyMapper.updateDislikeCount(replyId, -1);
                    }
                    return RestBean.success("已取消");
                } else {
                    // 切换类型
                    if (existing.getType() == 1) {
                        communityReplyMapper.updateLikeCount(replyId, -1);
                        communityReplyMapper.updateDislikeCount(replyId, 1);
                    } else {
                        communityReplyMapper.updateDislikeCount(replyId, -1);
                        communityReplyMapper.updateLikeCount(replyId, 1);
                    }
                    existing.setType(type);
                    communityReplyLikeMapper.insertOrUpdate(existing);
                    return RestBean.success(type == 1 ? "已点赞" : "已点踩");
                }
            } else {
                // 新操作
                CommunityReplyLike like = new CommunityReplyLike();
                like.setUserId(user.getId());
                like.setReplyId(replyId);
                like.setType(type);
                communityReplyLikeMapper.insertOrUpdate(like);
                if (type == 1) {
                    communityReplyMapper.updateLikeCount(replyId, 1);
                } else {
                    communityReplyMapper.updateDislikeCount(replyId, 1);
                }
                return RestBean.success(type == 1 ? "已点赞" : "已点踩");
            }
        } catch (Exception e) {
            logger.error("回复点赞/点踩失败", e);
            return RestBean.failure(500, "操作失败");
        }
    }

    @GetMapping("/community/reply/like-status")
    public RestBean<CommunityReplyLike> getReplyLikeStatus(@RequestParam Integer replyId,
                                                           HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.success(null);
            }
            CommunityReplyLike like = communityReplyLikeMapper.findByUserAndReply(user.getId(), replyId);
            return RestBean.success(like);
        } catch (Exception e) {
            return RestBean.success(null);
        }
    }

    @GetMapping("/community/reply/like-statuses")
    public RestBean<List<CommunityReplyLike>> getReplyLikeStatuses(@RequestParam List<Integer> replyIds,
                                                                    HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.success(Collections.emptyList());
            }
            List<CommunityReplyLike> likes = communityReplyLikeMapper.findByUserAndReplyIds(user.getId(), replyIds);
            return RestBean.success(likes);
        } catch (Exception e) {
            return RestBean.success(Collections.emptyList());
        }
    }

    @PostMapping("/community/reply/delete")
    public RestBean<String> deleteReply(@RequestParam Integer replyId,
                                        HttpSession session) {
        try {
            AccountUser user = (AccountUser) session.getAttribute("account");
            if (user == null) {
                return RestBean.failure(401, "请先登录");
            }

            // 查询回复信息，验证权限
            CommunityReply reply = communityReplyMapper.findWithReviewUserById(replyId);
            if (reply == null) {
                return RestBean.failure(404, "回复不存在");
            }

            // 权限验证：回复作者 或 评价作者 可以删除
            if (!reply.getUserId().equals(user.getId()) && !reply.getReviewUserId().equals(user.getId())) {
                return RestBean.failure(403, "无权删除");
            }

            // 先将有子回复的 parent_reply_id 置为 null
            communityReplyMapper.nullifyParentReplyId(replyId);

            // 删除回复
            int rows = communityReplyMapper.deleteReply(replyId);
            if (rows > 0) {
                return RestBean.success("删除成功");
            } else {
                return RestBean.failure(500, "删除失败");
            }
        } catch (Exception e) {
            logger.error("删除回复失败", e);
            return RestBean.failure(500, "删除失败");
        }
    }

}