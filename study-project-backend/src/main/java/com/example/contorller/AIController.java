package com.example.contorller;

import com.example.entity.Course;
import com.example.entity.RestBean;
import com.example.entity.user.AccountUser;
import com.example.entity.UserCourseRelation;
import com.example.mapper.UserMapper;
import com.example.service.CourseService;
import jakarta.annotation.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Resource
    private CourseService courseService;

    @Resource
    private UserMapper userMapper;

    @PostMapping("/chat")
    public RestBean<String> chat(@RequestBody Map<String, String> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
                return new RestBean<>(401, false, "请先登录后再使用AI助手功能。");
            }
            
            User springUser = (User) authentication.getPrincipal();
            AccountUser accountUser = userMapper.findAccountUserByNameOrEmail(springUser.getUsername());
            
            if (accountUser == null) {
                return new RestBean<>(401, false, "用户信息不存在，请重新登录。");
            }
            
            String message = request.get("message");
            if (message == null || message.trim().isEmpty()) {
                return new RestBean<>(400, false, "消息内容不能为空");
            }
            
            String response = generateAIResponse(message, accountUser.getId());
            return new RestBean<>(200, true, response);
        } catch (Exception e) {
            e.printStackTrace();
            return new RestBean<>(500, false, "服务器内部错误: " + e.getMessage());
        }
    }
    
    private String generateAIResponse(String message, Integer userId) {
        String lowerMessage = message.toLowerCase();
        
        try {
            if (isShowAllRequest(lowerMessage)) {
                return handleShowAll(message, userId);
            } else if (isSortingRequest(lowerMessage)) {
                return handleSortingRequest(message, userId);
            } else if (isFilteringRequest(lowerMessage)) {
                return handleFilteringRequest(message, userId);
            } else if (isPaginationRequest(lowerMessage)) {
                return handlePaginationRequest(message, userId);
            } else if (isCheckInRequest(lowerMessage)) {
                return handleCheckInRequest(message, userId);
            } else if (isEnrollmentRequest(lowerMessage)) {
                return handleEnrollment(message, userId);
            } else if (isUnenrollmentRequest(lowerMessage)) {
                return handleUnenrollment(message, userId);
            } else if (isCourseQueryRequest(lowerMessage)) {
                return handleCourseQuery(message, userId);
            } else if (lowerMessage.contains("你好") || lowerMessage.contains("hello")) {
                return "你好！很高兴见到你！有什么我可以帮助你的吗？你可以问我关于课程的问题，比如：\n- 我有哪些课没报\n- 帮我报名[课程名]\n- 查看我的课程\n- 帮我签到[课程名]\n- 帮我退课[课程名]\n- 按评分排序";
            } else if (lowerMessage.contains("帮助") || lowerMessage.contains("help") 
                    || lowerMessage.contains("功能") || lowerMessage.contains("能做什么") 
                    || lowerMessage.contains("有什么用") || lowerMessage.contains("有哪些功能")
                    || lowerMessage.contains("你会什么")) {
                return "🎯 我能帮你做的事情：\n\n" +
                        "📚 【课程查询】\n" +
                        "  • 查看未报的课程：我有哪些课没报\n" +
                        "  • 查看已报课程：我的课程、查看已报名\n" +
                        "  • 查看所有课程：全部课程、显示所有\n\n" +
                        "📝 【课程报名】\n" +
                        "  • 报名课程：帮我报名[课程名]\n" +
                        "  • 取消报名：帮我退课[课程名]\n\n" +
                        "✅ 【签到打卡】\n" +
                        "  • 签到：帮我签到[课程名]、打卡[课程名]\n\n" +
                        "🔍 【筛选排序】\n" +
                        "  • 按评分排序：按评分排序、评分最高的课程\n" +
                        "  • 按热门排序：最受欢迎的课程、学生最多的\n" +
                        "  • 按教师筛选：查看张老师的课程\n\n" +
                        "📄 【分页查看】\n" +
                        "  • 下一页：第2页、下一页、更多\n\n" +
                        "❓ 你想做什么？直接告诉我就好！";
            } else if (lowerMessage.contains("谢谢") || lowerMessage.contains("感谢")) {
                return "不客气！很高兴能帮助到你。如果还有其他问题，随时可以问我。";
            } else if (lowerMessage.contains("再见") || lowerMessage.contains("拜拜")) {
                return "再见！祝你学习愉快，有需要随时回来找我。";
            } else if (lowerMessage.contains("时间") || lowerMessage.contains("几点")) {
                return "现在是 " + java.time.LocalDateTime.now().toString().substring(0, 16) + "。";
            } else if (lowerMessage.contains("天气")) {
                return "抱歉，我暂时无法获取实时天气信息。建议你查看天气应用或网站获取准确的天气预报。";
            } else if (lowerMessage.contains("作业") || lowerMessage.contains("考试")) {
                return "关于作业和考试，建议你：\n1. 合理安排时间，制定学习计划\n2. 及时复习课程内容\n3. 遇到问题及时向老师或同学请教\n4. 保持良好的作息习惯";
            } else if (lowerMessage.contains("技术") || lowerMessage.contains("编程") || lowerMessage.contains("代码")) {
                return "关于技术学习，我建议：\n1. 从基础开始，打好理论基础\n2. 多动手实践，编写代码\n3. 阅读优秀的开源项目\n4. 参与技术社区和论坛\n5. 持续学习新技术";
            } else {
                return "我理解你的问题。作为AI助手，我会尽力帮助你。不过我的回答可能不够准确，建议你：\n\n1. 提供更多具体信息\n2. 咨询相关专业人士\n3. 查阅权威资料\n\n有什么其他我可以帮助你的吗？";
            }
        } catch (Exception e) {
            return "抱歉，处理你的请求时出现了错误：" + e.getMessage() + "。请稍后再试。";
        }
    }
    
    private boolean isEnrollmentRequest(String message) {
        String[] enrollmentKeywords = {
                "报名", "选课", "加入", "选修", "注册", "申请",
                "想报", "要报", "想选", "要选", "想学", "要学",
                "帮我报名", "帮我选课", "帮我加入", "帮我选修", "帮我注册",
                "给我报名", "给我选课", "给我加入", "给我选修", "给我注册",
                "我要报名", "我要选课", "我要加入", "我要选修", "我要注册",
                "去报名", "去选课", "去加入", "去选修", "去注册"
        };
        
        for (String keyword : enrollmentKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isShowAllRequest(String message) {
        String[] showAllKeywords = {
                "显示全部", "全部显示", "显示所有", "所有显示",
                "查看全部", "全部查看", "查看所有", "所有查看",
                "展开全部", "全部展开", "展开所有", "所有展开",
                "show all", "display all", "view all"
        };
        
        for (String keyword : showAllKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isSortingRequest(String message) {
        String[] sortingKeywords = {
                "排序", "按", "从高到低", "从低到高", "最高", "最低",
                "最热门", "最受欢迎", "评价最好", "评分最高", "学生最多",
                "sort by", "order by", "desc", "asc"
        };
        
        for (String keyword : sortingKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isFilteringRequest(String message) {
        String[] filteringKeywords = {
                "筛选", "过滤", "找", "只看", "只要",
                "老师", "教师的", "哪位老师", "哪个老师",
                "教师", "教师的", "filter", "by teacher"
        };
        
        for (String keyword : filteringKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isPaginationRequest(String message) {
        String[] paginationKeywords = {
                "第", "页", "更多", " 下一页", "下一批",
                "page", "next"
        };
        
        for (String keyword : paginationKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isCheckInRequest(String message) {
        String[] checkInKeywords = {
                "签到", "打卡", "签到打卡",
                "想签到", "要签到", "帮我签到",
                "给我签到", "我要签到", "去签到"
        };
        
        for (String keyword : checkInKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private String handleCheckInRequest(String message, Integer userId) {
        try {
            List<UserCourseRelation> userCourses = courseService.getUserCourses(userId);
            
            if (userCourses.isEmpty()) {
                return "你还没有报名任何课程，无法签到。请先报名一些课程吧！";
            }
            
            String courseName = extractCourseName(message);
            
            if (courseName == null || courseName.trim().isEmpty()) {
                StringBuilder response = new StringBuilder("请告诉我想给哪个课程签到。你已报名的课程：\n\n");
                for (int i = 0; i < userCourses.size() && i < 5; i++) {
                    UserCourseRelation relation = userCourses.get(i);
                    Course course = relation.getCourse();
                    if (course != null) {
                        String checkInStatus = "";
                        if (relation.getCheckInCount() != null && relation.getMaxCheckInCount() != null) {
                            checkInStatus = String.format(" (已签到 %d/%d 次)", 
                                    relation.getCheckInCount(), relation.getMaxCheckInCount());
                        }
                        response.append(String.format("%d. %s%s\n", i + 1, course.getName(), checkInStatus));
                    }
                }
                response.append("\n请对我说\"帮我签到[课程名]\"。");
                return response.toString();
            }
            
            Course targetCourse = findCourseByName(courseName, 
                    userCourses.stream()
                            .map(UserCourseRelation::getCourse)
                            .filter(course -> course != null)
                            .collect(Collectors.toList()));
            
            if (targetCourse == null) {
                return "抱歉，你还没有报名课程\"" + courseName + "\"。请检查课程名称是否正确。";
            }
            
            UserCourseRelation targetRelation = userCourses.stream()
                    .filter(r -> r.getCourseId().equals(targetCourse.getId()))
                    .findFirst()
                    .orElse(null);
            
            if (targetRelation == null) {
                return "找不到你报名课程\"" + courseName + "\"的记录。";
            }
            
            if (targetRelation.getLastCheckInDate() != null) {
                java.time.LocalDate today = java.time.LocalDate.now();
                java.time.LocalDate lastCheckIn = targetRelation.getLastCheckInDate().toLocalDate();
                if (lastCheckIn.equals(today)) {
                    int checkInCount = targetRelation.getCheckInCount() != null ? targetRelation.getCheckInCount() : 0;
                    int maxCount = targetRelation.getMaxCheckInCount() != null ? targetRelation.getMaxCheckInCount() : 12;
                    return "你今天已经签到过\"" + targetCourse.getName() + "\"了！\n\n当前进度：" + checkInCount + "/" + maxCount + "次";
                }
            }
            
            boolean success = courseService.checkIn(userId, targetCourse.getId());
            
            if (success) {
                List<UserCourseRelation> updatedCourses = courseService.getUserCourses(userId);
                UserCourseRelation updatedRelation = updatedCourses.stream()
                        .filter(r -> r.getCourseId().equals(targetCourse.getId()))
                        .findFirst()
                        .orElse(null);
                
                int checkInCount = updatedRelation != null && updatedRelation.getCheckInCount() != null 
                        ? updatedRelation.getCheckInCount() : 1;
                int maxCount = updatedRelation != null && updatedRelation.getMaxCheckInCount() != null 
                        ? updatedRelation.getMaxCheckInCount() : 12;
                
                StringBuilder response = new StringBuilder();
                response.append("✅ 签到成功！\n\n");
                response.append("课程：").append(targetCourse.getName()).append("\n");
                response.append("签到次数：").append(checkInCount).append("/").append(maxCount).append("次\n");
                
                if (checkInCount >= maxCount) {
                    response.append("\n🎉 恭喜！你已完成全部签到！");
                } else {
                    response.append("\n继续加油！记得每天签到哦～");
                }
                
                return response.toString();
            } else {
                return "签到失败，请稍后再试。";
            }
        } catch (Exception e) {
            return "签到时出错：" + e.getMessage() + "。请稍后再试。";
        }
    }
    
    private String handleShowAll(String message, Integer userId) {
        try {
            List<Course> allCourses = courseService.getAllCourses();
            List<UserCourseRelation> userCourses = courseService.getUserCourses(userId);
            
            List<Integer> enrolledCourseIds = userCourses.stream()
                    .map(UserCourseRelation::getCourseId)
                    .collect(Collectors.toList());
            
            List<Course> notEnrolledCourses = allCourses.stream()
                    .filter(course -> !enrolledCourseIds.contains(course.getId()))
                    .collect(Collectors.toList());
            
            if (notEnrolledCourses.isEmpty()) {
                return "你已经报名了所有可用的课程！太棒了！";
            }
            
            StringBuilder response = new StringBuilder("所有可用的课程：\n\n");
            for (int i = 0; i < notEnrolledCourses.size(); i++) {
                Course course = notEnrolledCourses.get(i);
                response.append(String.format("%d. %s\n   教师：%s\n   时长：%s\n   评分：%.1f\n\n", 
                        i + 1, course.getName(), course.getTeacherName(), course.getDuration(), course.getRating()));
            }
            response.append("你可以对我说\"帮我报名[课程名]\"来报名。");
            return response.toString();
        } catch (Exception e) {
            return "显示全部课程时出错：" + e.getMessage() + "。请稍后再试。";
        }
    }
    
    private String handleSortingRequest(String message, Integer userId) {
        try {
            String sortBy = "rating";
            String order = "desc";
            String lowerMessage = message.toLowerCase();
            
            if (lowerMessage.contains("评分") || lowerMessage.contains("评价")) {
                if (lowerMessage.contains("低") || lowerMessage.contains("从低到高") || lowerMessage.contains("asc")) {
                    order = "asc";
                } else {
                    order = "desc";
                }
                sortBy = "rating";
            } else if (lowerMessage.contains("学生") || lowerMessage.contains("热门") || lowerMessage.contains("受欢迎")) {
                if (lowerMessage.contains("少") || lowerMessage.contains("从少到多")) {
                    order = "asc";
                } else {
                    order = "desc";
                }
                sortBy = "students";
            } else if (lowerMessage.contains("时长")) {
                if (lowerMessage.contains("长") || lowerMessage.contains("从长到短")) {
                    order = "desc";
                } else {
                    order = "asc";
                }
                sortBy = "duration";
            } else if (lowerMessage.contains("名字") || lowerMessage.contains("名称")) {
                sortBy = "name";
                order = "asc";
            }
            
            List<Course> courses = courseService.getCoursesSorted(sortBy, order);
            
            if (courses.isEmpty()) {
                return "目前没有可用的课程。";
            }
            
            StringBuilder response = new StringBuilder();
            String orderText = order.equals("desc") ? "从高到低" : "从低到高";
            String sortText = sortBy.equals("rating") ? "评分" : (sortBy.equals("students") ? "学生数" : "时长");
            response.append("按").append(sortText).append("排序（").append(orderText).append("）：\n\n");
            
            for (int i = 0; i < courses.size() && i < 10; i++) {
                Course course = courses.get(i);
                response.append(String.format("%d. %s\n   教师：%s\n   评分：%.1f⭐  学生：%d人\n   时长：%s\n\n", 
                        i + 1, course.getName(), course.getTeacherName(), 
                        course.getRating() != null ? course.getRating() : 0,
                        course.getStudentsCount() != null ? course.getStudentsCount() : 0,
                        course.getDuration()));
            }
            
            if (courses.size() > 10) {
                response.append(String.format("还有 %d 门课程未显示...你可以对我说\"第2页\"查看更多。\n", courses.size() - 10));
            }
            
            return response.toString();
        } catch (Exception e) {
            return "排序查询时出错：" + e.getMessage() + "。请稍后再试。";
        }
    }
    
    private String handleFilteringRequest(String message, Integer userId) {
        try {
            String teacherName = extractTeacherName(message);
            
            if (teacherName == null || teacherName.trim().isEmpty()) {
                List<String> teachers = courseService.getAllTeachers();
                if (teachers.isEmpty()) {
                    return "目前没有可用的教师。";
                }
                StringBuilder response = new StringBuilder("可选的教师有：\n\n");
                for (int i = 0; i < teachers.size(); i++) {
                    response.append(String.format("%d. %s\n", i + 1, teachers.get(i)));
                }
                response.append("\n你可以对我说\"查看[老师姓名]的课程\"来筛选。");
                return response.toString();
            }
            
            List<Course> courses = courseService.getCoursesByTeacher(teacherName);
            
            if (courses.isEmpty()) {
                return "没有找到教师\"" + teacherName + "\"的课程。";
            }
            
            StringBuilder response = new StringBuilder("教师「" + teacherName + "」的课程：\n\n");
            for (int i = 0; i < courses.size(); i++) {
                Course course = courses.get(i);
                response.append(String.format("%d. %s\n   评分：%.1f⭐  学生：%d人\n   时长：%s\n\n", 
                        i + 1, course.getName(), 
                        course.getRating() != null ? course.getRating() : 0,
                        course.getStudentsCount() != null ? course.getStudentsCount() : 0,
                        course.getDuration()));
            }
            
            return response.toString();
        } catch (Exception e) {
            return "筛选课程时出错：" + e.getMessage() + "。请稍后再试。";
        }
    }
    
    private String handlePaginationRequest(String message, Integer userId) {
        try {
            int page = extractPageNumber(message);
            int pageSize = 10;
            
            List<Course> courses = courseService.getCoursesPaginated(page, pageSize);
            
            if (courses.isEmpty()) {
                return "没有更多课程了。";
            }
            
            StringBuilder response = new StringBuilder("第").append(page + 1).append("页课程：\n\n");
            for (int i = 0; i < courses.size(); i++) {
                Course course = courses.get(i);
                response.append(String.format("%d. %s\n   教师：%s\n   评分：%.1f⭐  学生：%d人\n   时长：%s\n\n", 
                        i + 1, course.getName(), course.getTeacherName(),
                        course.getRating() != null ? course.getRating() : 0,
                        course.getStudentsCount() != null ? course.getStudentsCount() : 0,
                        course.getDuration()));
            }
            
            response.append("你可以继续说\"第").append(page + 2).append("页\"查看更多。");
            
            return response.toString();
        } catch (Exception e) {
            return "分页查询时出错：" + e.getMessage() + "。请稍后再试。";
        }
    }
    
    private String extractTeacherName(String message) {
        String[] patterns = {
                "查看\\s*(.+)的课程",
                "筛选\\s*(.+)的课程",
                "只看\\s*(.+)老师",
                "只看\\s*(.+)教师",
                "哪位老师\\s*(.+)",
                "哪个老师\\s*(.+)",
                "老师\\s*(.+)",
                "教师\\s*(.+)",
                "找\\s*(.+)老师",
                "找\\s*(.+)教师",
                "by teacher\\s*(.+)",
                "teacher\\s*(.+)"
        };
        
        for (String pattern : patterns) {
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = regex.matcher(message);
            if (matcher.find()) {
                String teacherName = matcher.group(1).trim();
                if (!teacherName.isEmpty()) {
                    return teacherName;
                }
            }
        }
        
        return null;
    }
    
    private int extractPageNumber(String message) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("第\\s*(\\d+)\\s*页");
        java.util.regex.Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1)) - 1;
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
    
    private boolean isUnenrollmentRequest(String message) {
        String[] unenrollmentKeywords = {
                "退课", "退选", "取消", "撤销", "退出",
                "想退", "要退", "想取消", "要撤销", "想退出",
                "帮我退课", "帮我退选", "帮我取消", "帮我撤销", "帮我退出",
                "给我退课", "给我退选", "给我取消", "给我撤销", "给我退出",
                "我要退课", "我要退选", "我要取消", "我要撤销", "我要退出",
                "去退课", "去退选", "去取消", "去撤销", "去退出"
        };
        
        for (String keyword : unenrollmentKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isCourseQueryRequest(String message) {
        String[] courseQueryKeywords = {
                "课程", "课", "学习", "上课", "听课",
                "我的课", "我的课程", "已报课", "已报课程", "已经报课", "已经报课程",
                "没报课", "没报课程", "未报课", "未报课程", "没有报课", "没有报课程",
                "有什么课", "有哪些课", "什么课", "哪些课",
                "查看课程", "看看课程", "显示课程", "列出课程"
        };
        
        for (String keyword : courseQueryKeywords) {
            if (message.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
    
    private String handleCourseQuery(String message, Integer userId) {
        try {
            List<Course> allCourses = courseService.getAllCourses();
            List<UserCourseRelation> userCourses = courseService.getUserCourses(userId);
            
            List<Integer> enrolledCourseIds = userCourses.stream()
                    .map(UserCourseRelation::getCourseId)
                    .collect(Collectors.toList());
            
            List<Course> notEnrolledCourses = allCourses.stream()
                    .filter(course -> !enrolledCourseIds.contains(course.getId()))
                    .collect(Collectors.toList());
            
            if (message.contains("没报") || message.contains("未报") || message.contains("没有报")) {
                if (notEnrolledCourses.isEmpty()) {
                    return "你已经报名了所有可用的课程！太棒了！";
                } else {
                    StringBuilder response = new StringBuilder("你还没有报名以下课程：\n\n");
                    for (int i = 0; i < notEnrolledCourses.size(); i++) {
                        Course course = notEnrolledCourses.get(i);
                        response.append(String.format("%d. %s\n   教师：%s\n   时长：%s\n\n", 
                                i + 1, course.getName(), course.getTeacherName(), course.getDuration()));
                    }
                    response.append("你可以对我说\"帮我报名[课程名]\"来报名。");
                    return response.toString();
                }
            } else if (message.contains("我的") || message.contains("已报") || message.contains("已经报")) {
                if (userCourses.isEmpty()) {
                    return "你还没有报名任何课程。快去选择一些感兴趣的课程吧！";
                } else {
                    StringBuilder response = new StringBuilder("你已报名的课程：\n\n");
                    for (int i = 0; i < userCourses.size(); i++) {
                        UserCourseRelation relation = userCourses.get(i);
                        Course course = relation.getCourse();
                        if (course != null) {
                            response.append(String.format("%d. %s\n   进度：%d%%\n   签到：%d次\n\n", 
                                    i + 1, course.getName(), relation.getProgress(), relation.getCheckInCount()));
                        }
                    }
                    return response.toString();
                }
            } else {
                if (allCourses.isEmpty()) {
                    return "目前没有可用的课程。";
                } else {
                    StringBuilder response = new StringBuilder("当前可用的课程：\n\n");
                    for (int i = 0; i < allCourses.size(); i++) {
                        Course course = allCourses.get(i);
                        response.append(String.format("%d. %s\n   教师：%s\n   时长：%s\n   评分：%.1f\n\n", 
                                i + 1, course.getName(), course.getTeacherName(), 
                                course.getDuration(), course.getRating()));
                    }
                    return response.toString();
                }
            }
        } catch (Exception e) {
            return "查询课程信息时出错：" + e.getMessage() + "。请稍后再试。";
        }
    }
    
    private String handleEnrollment(String message, Integer userId) {
        try {
            List<Course> allCourses = courseService.getAllCourses();
            List<UserCourseRelation> userCourses = courseService.getUserCourses(userId);
            
            List<Integer> enrolledCourseIds = userCourses.stream()
                    .map(UserCourseRelation::getCourseId)
                    .collect(Collectors.toList());
            
            List<Course> notEnrolledCourses = allCourses.stream()
                    .filter(course -> !enrolledCourseIds.contains(course.getId()))
                    .collect(Collectors.toList());
            
            if (notEnrolledCourses.isEmpty()) {
                return "你已经报名了所有可用的课程！";
            }
            
            String courseName = extractCourseName(message);
            if (courseName == null || courseName.trim().isEmpty()) {
                StringBuilder response = new StringBuilder("请告诉我你想报名哪门课程。你可以选择：\n\n");
                for (int i = 0; i < notEnrolledCourses.size() && i < 3; i++) {
                    Course course = notEnrolledCourses.get(i);
                    response.append(String.format("%d. %s\n", i + 1, course.getName()));
                }
                if (notEnrolledCourses.size() > 3) {
                    response.append(String.format("还有 %d 门课程可选...\n", notEnrolledCourses.size() - 3));
                }
                response.append("请对我说\"帮我报名[课程名]\"。");
                return response.toString();
            }
            
            Course targetCourse = findCourseByName(courseName, notEnrolledCourses);
            if (targetCourse == null) {
                return "抱歉，没有找到名为\"" + courseName + "\"的课程。请检查课程名称是否正确。";
            }
            
            boolean success = courseService.enrollCourse(userId, targetCourse.getId());
            if (success) {
                return "恭喜！你已成功报名课程：" + targetCourse.getName() + "。\n\n课程信息：\n教师：" + targetCourse.getTeacherName() + "\n时长：" + targetCourse.getDuration() + "\n\n记得按时签到哦！";
            } else {
                return "报名失败，可能你已经报名了这门课程。请查看你的已报课程列表。";
            }
        } catch (Exception e) {
            return "报名课程时出错：" + e.getMessage() + "。请稍后再试。";
        }
    }
    
    private String handleUnenrollment(String message, Integer userId) {
        try {
            List<UserCourseRelation> userCourses = courseService.getUserCourses(userId);
            
            if (userCourses.isEmpty()) {
                return "你还没有报名任何课程，无法退课。";
            }
            
            String courseName = extractCourseName(message);
            if (courseName == null || courseName.trim().isEmpty()) {
                StringBuilder response = new StringBuilder("请告诉我你想退哪门课程。你已报名的课程：\n\n");
                for (int i = 0; i < userCourses.size() && i < 5; i++) {
                    UserCourseRelation relation = userCourses.get(i);
                    Course course = relation.getCourse();
                    if (course != null) {
                        response.append(String.format("%d. %s\n", i + 1, course.getName()));
                    }
                }
                if (userCourses.size() > 5) {
                    response.append(String.format("还有 %d 门课程未显示...\n", userCourses.size() - 5));
                }
                response.append("请对我说\"帮我退课[课程名]\"。");
                return response.toString();
            }
            
            Course targetCourse = findCourseByName(courseName, 
                    userCourses.stream()
                            .map(UserCourseRelation::getCourse)
                            .filter(course -> course != null)
                            .collect(Collectors.toList()));
            
            if (targetCourse == null) {
                return "抱歉，没有找到名为\"" + courseName + "\"的课程，或者你还没有报名这门课程。";
            }
            
            boolean success = courseService.unenrollCourse(userId, targetCourse.getId());
            if (success) {
                return "已成功退课：" + targetCourse.getName() + "。\n\n退课成功！如果以后想重新学习，可以随时再次报名。";
            } else {
                return "退课失败，可能你还没有报名这门课程。请查看你的已报课程列表。";
            }
        } catch (Exception e) {
            return "退课时出错：" + e.getMessage() + "。请稍后再试。";
        }
    }
    
    private String extractCourseName(String message) {
        String lowerMessage = message.toLowerCase();
        
        String[] patterns = {
                "报名\\s*(.+)",
                "选课\\s*(.+)",
                "加入\\s*(.+)",
                "选修\\s*(.+)",
                "注册\\s*(.+)",
                "申请\\s*(.+)",
                "退课\\s*(.+)",
                "退选\\s*(.+)",
                "取消\\s*(.+)",
                "撤销\\s*(.+)",
                "退出\\s*(.+)",
                "签到\\s*(.+)",
                "打卡\\s*(.+)",
                "帮我报名\\s*(.+)",
                "帮我选课\\s*(.+)",
                "帮我加入\\s*(.+)",
                "帮我选修\\s*(.+)",
                "帮我注册\\s*(.+)",
                "帮我退课\\s*(.+)",
                "帮我退选\\s*(.+)",
                "帮我取消\\s*(.+)",
                "帮我撤销\\s*(.+)",
                "帮我退出\\s*(.+)",
                "帮我签到\\s*(.+)",
                "帮我打卡\\s*(.+)",
                "给我报名\\s*(.+)",
                "给我选课\\s*(.+)",
                "给我加入\\s*(.+)",
                "给我选修\\s*(.+)",
                "给我注册\\s*(.+)",
                "给我退课\\s*(.+)",
                "给我退选\\s*(.+)",
                "给我取消\\s*(.+)",
                "给我撤销\\s*(.+)",
                "给我退出\\s*(.+)",
                "给我签到\\s*(.+)",
                "给我打卡\\s*(.+)",
                "我要报名\\s*(.+)",
                "我要选课\\s*(.+)",
                "我要加入\\s*(.+)",
                "我要选修\\s*(.+)",
                "我要注册\\s*(.+)",
                "我要退课\\s*(.+)",
                "我要退选\\s*(.+)",
                "我要取消\\s*(.+)",
                "我要撤销\\s*(.+)",
                "我要退出\\s*(.+)",
                "我要签到\\s*(.+)",
                "我要打卡\\s*(.+)",
                "去报名\\s*(.+)",
                "去选课\\s*(.+)",
                "去加入\\s*(.+)",
                "去选修\\s*(.+)",
                "去注册\\s*(.+)",
                "去退课\\s*(.+)",
                "去退选\\s*(.+)",
                "去取消\\s*(.+)",
                "去撤销\\s*(.+)",
                "去退出\\s*(.+)",
                "去签到\\s*(.+)",
                "去打卡\\s*(.+)"
        };
        
        for (String pattern : patterns) {
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = regex.matcher(message);
            if (matcher.find()) {
                String courseName = matcher.group(1).trim();
                if (!courseName.isEmpty()) {
                    return courseName;
                }
            }
        }
        
        return null;
    }
    
    private Course findCourseByName(String courseName, List<Course> courses) {
        String lowerCourseName = courseName.toLowerCase().trim();
        
        for (Course course : courses) {
            if (course.getName().toLowerCase().contains(lowerCourseName)) {
                return course;
            }
        }
        
        return null;
    }
}
