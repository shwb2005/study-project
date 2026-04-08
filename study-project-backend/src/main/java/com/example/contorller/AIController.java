package com.example.contorller;

import com.example.entity.Course;
import com.example.entity.RestBean;
import com.example.entity.user.AccountUser;
import com.example.entity.UserCourseRelation;
import com.example.mapper.UserMapper;
import com.example.service.CourseService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    private static final Logger logger = LoggerFactory.getLogger(AIController.class);

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.model}")
    private String model;

    @Resource
    private CourseService courseService;

    @Resource
    private UserMapper userMapper;

    private final RestTemplate restTemplate;
    
    public AIController() {
        this.restTemplate = new RestTemplate();
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(60000);
        this.restTemplate.setRequestFactory(factory);
    }

    @PostMapping("/chat")
    public RestBean<String> chat(HttpServletRequest request) {
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
            
            String message = request.getParameter("message");
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
        try {
            String systemPrompt = buildSystemPrompt(userId);
            return callLLM(systemPrompt, message);
        } catch (Exception e) {
            return "抱歉，AI服务暂时不可用：" + e.getMessage();
        }
    }

    private String buildSystemPrompt(Integer userId) {
        List<Course> allCourses = courseService.getAllCourses();
        List<UserCourseRelation> userCourses = courseService.getUserCourses(userId);
        
        List<Integer> enrolledCourseIds = userCourses.stream()
                .map(UserCourseRelation::getCourseId)
                .collect(java.util.stream.Collectors.toList());
        
        List<Course> notEnrolledCourses = allCourses.stream()
                .filter(course -> !enrolledCourseIds.contains(course.getId()))
                .collect(java.util.stream.Collectors.toList());
        
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是课程推荐助手，专门帮助用户选择和报名课程。\n\n");
        prompt.append("【你的职责】\n");
        prompt.append("1. 根据用户需求推荐合适的课程\n");
        prompt.append("2. 回答用户关于课程的问题\n");
        prompt.append("3. 帮助用户完成报名、签到等操作\n");
        prompt.append("4. 提供选课建议和学习规划\n\n");
        
        prompt.append("【所有课程信息】\n");
        for (Course course : allCourses) {
            prompt.append(String.format("- %s | 教师:%s | 评分:%.1f | 学生:%d | 时长:%s\n",
                    course.getName(),
                    course.getTeacherName(),
                    course.getRating() != null ? course.getRating() : 0,
                    course.getStudentsCount() != null ? course.getStudentsCount() : 0,
                    course.getDuration()));
        }
        prompt.append("\n");
        
        prompt.append("【用户已报名的课程】\n");
        if (userCourses.isEmpty()) {
            prompt.append("用户尚未报名任何课程\n");
        } else {
            for (UserCourseRelation relation : userCourses) {
                Course course = relation.getCourse();
                if (course != null) {
                    prompt.append(String.format("- %s | 进度:%d%% | 签到:%d/%d次\n",
                            course.getName(),
                            relation.getProgress() != null ? relation.getProgress() : 0,
                            relation.getCheckInCount() != null ? relation.getCheckInCount() : 0,
                            relation.getMaxCheckInCount() != null ? relation.getMaxCheckInCount() : 12));
                }
            }
        }
        prompt.append("\n");
        
        prompt.append("【可报名的课程】\n");
        if (notEnrolledCourses.isEmpty()) {
            prompt.append("用户已报名所有课程\n");
        } else {
            for (Course course : notEnrolledCourses) {
                prompt.append(String.format("- %s | 教师:%s | 评分:%.1f\n",
                        course.getName(),
                        course.getTeacherName(),
                        course.getRating() != null ? course.getRating() : 0));
            }
        }
        prompt.append("\n");
        
        prompt.append("【回复要求】\n");
        prompt.append("1. 根据用户已报名的课程和可报名课程进行推荐\n");
        prompt.append("2. 回答简洁有条理，突出课程特色\n");
        prompt.append("3. 如果用户想报名某课程，告诉用户使用 /api/course/enroll 接口\n");
        prompt.append("4. 如果用户想签到，告诉用户使用 /api/course/check-in 接口\n");
        prompt.append("5. 适当使用emoji增加可读性\n");
        prompt.append("6. 优先推荐评分高、学生多的热门课程\n");
        
        return prompt.toString();
    }

    private String callLLM(String systemPrompt, String userMessage) {
        try {
            logger.info("开始调用大模型API, baseUrl: {}, model: {}", baseUrl, model);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);
            
            Map<String, Object> body = new HashMap<>();
            body.put("model", model);
            
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMsg = new HashMap<>();
            systemMsg.put("role", "system");
            systemMsg.put("content", systemPrompt);
            messages.add(systemMsg);
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);
            
            body.put("messages", messages);
            body.put("temperature", 0.7);
            body.put("max_tokens", 1000);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            logger.info("发送请求到: {}", baseUrl + "/chat/completions");
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    baseUrl + "/chat/completions",
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            
            logger.info("收到响应, status: {}", response.getStatusCode());
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<?, ?> responseBody = response.getBody();
                List<?> choices = (List<?>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<?, ?> choice = (Map<?, ?>) choices.get(0);
                    Map<?, ?> message = (Map<?, ?>) choice.get("message");
                    if (message != null) {
                        String content = (String) message.get("content");
                        logger.info("AI响应内容: {}", content);
                        return content;
                    }
                }
            }
            
            logger.warn("AI响应格式异常, body: {}", response.getBody());
            return "AI服务响应格式异常";
        } catch (Exception e) {
            logger.error("调用AI服务失败: ", e);
            return "调用AI服务失败：" + e.getMessage();
        }
    }

    private boolean isShowAllRequest(String message) {
        String[] showAllKeywords = {"显示全部", "全部显示", "显示所有", "所有显示", "查看全部", "全部查看", "查看所有", "所有查看"};
        for (String keyword : showAllKeywords) {
            if (message.contains(keyword)) return true;
        }
        return false;
    }

    private boolean isSortingRequest(String message) {
        String[] sortingKeywords = {"排序", "按", "从高到低", "从低到高", "最高", "最低", "最热门", "最受欢迎"};
        for (String keyword : sortingKeywords) {
            if (message.contains(keyword)) return true;
        }
        return false;
    }

    private boolean isFilteringRequest(String message) {
        String[] filteringKeywords = {"筛选", "过滤", "只看", "老师", "教师"};
        for (String keyword : filteringKeywords) {
            if (message.contains(keyword)) return true;
        }
        return false;
    }

    private boolean isCheckInRequest(String message) {
        String[] checkInKeywords = {"签到", "打卡"};
        for (String keyword : checkInKeywords) {
            if (message.contains(keyword)) return true;
        }
        return false;
    }

    private boolean isEnrollmentRequest(String message) {
        String[] enrollmentKeywords = {"报名", "选课", "加入", "选修", "注册", "申请"};
        for (String keyword : enrollmentKeywords) {
            if (message.contains(keyword)) return true;
        }
        return false;
    }

    private boolean isUnenrollmentRequest(String message) {
        String[] unenrollmentKeywords = {"退课", "退选", "取消", "撤销", "退出"};
        for (String keyword : unenrollmentKeywords) {
            if (message.contains(keyword)) return true;
        }
        return false;
    }

    private boolean isCourseQueryRequest(String message) {
        String[] courseQueryKeywords = {"课程", "课", "学习", "我的课", "已报课", "没报课"};
        for (String keyword : courseQueryKeywords) {
            if (message.contains(keyword)) return true;
        }
        return false;
    }

    private String extractCourseName(String message) {
        String[] patterns = {
                "报名\\s*(.+)", "选课\\s*(.+)", "加入\\s*(.+)", "退课\\s*(.+)",
                "签到\\s*(.+)", "打卡\\s*(.+)", "帮我报名\\s*(.+)", "帮我选课\\s*(.+)",
                "帮我退课\\s*(.+)", "帮我签到\\s*(.+)"
        };
        
        for (String pattern : patterns) {
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = regex.matcher(message);
            if (matcher.find()) {
                String courseName = matcher.group(1).trim();
                if (!courseName.isEmpty()) return courseName;
            }
        }
        return null;
    }

    private String extractTeacherName(String message) {
        String[] patterns = {"查看\\s*(.+)的课程", "只看\\s*(.+)老师", "哪位老师\\s*(.+)"};
        for (String pattern : patterns) {
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = regex.matcher(message);
            if (matcher.find()) {
                return matcher.group(1).trim();
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