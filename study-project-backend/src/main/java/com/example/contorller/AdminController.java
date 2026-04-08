package com.example.contorller;


import com.example.entity.CommunityReply;
import com.example.entity.user.Admin;
import com.example.entity.RestBean;
import com.example.mapper.CommunityReplyMapper;
import com.example.service.AdminService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Resource
    private AdminService adminService;

    @Resource
    private CommunityReplyMapper communityReplyMapper;

    @PostMapping("/login")
    public RestBean<String> login(@RequestParam String username,
                                  @RequestParam String password,
                                  HttpSession session) {


        if (adminService.validateAdmin(username, password)) {
            Admin admin = adminService.findByUsername(username);

            // 直接使用 Admin 对象存储到 session
            session.setAttribute("admin", admin);


            return RestBean.success("管理员登录成功");
        } else {
            logger.warn("管理员登录失败 - 用户名: {}", username);
            return RestBean.failure(401, "管理员用户名或密码错误");
        }
    }

    @GetMapping("/me")
    public RestBean<Admin> getCurrentAdmin(HttpSession session) {
        Admin admin = (Admin) session.getAttribute("admin");


        if (admin != null) {
            // 返回 Admin 对象，但不包含密码
            Admin safeAdmin = new Admin();
            safeAdmin.setId(admin.getId());
            safeAdmin.setUsername(admin.getUsername());
            // 不设置密码字段
            return new RestBean<>(200, true, safeAdmin);
        } else {
            logger.warn("未找到管理员信息，可能未登录或session过期");
            return new RestBean<>(401, false, null);
        }
    }

    @PostMapping("/logout")
    public RestBean<String> logout(HttpSession session) {
        session.removeAttribute("admin");
        return RestBean.success("管理员已退出登录");
    }

    @GetMapping("/list")
    public RestBean<List<Admin>> getAdminList(HttpSession session) {

        // 打印session信息
        Admin sessionAdmin = (Admin) session.getAttribute("admin");

        try {
            List<Admin> admins = adminService.getAllAdmins();

            // 移除密码信息
            admins.forEach(admin -> admin.setPassword(null));
            return RestBean.success(admins);
        } catch (Exception e) {
            logger.error("获取管理员列表失败", e);
            return new RestBean(500, false, "获取管理员列表失败");
        }
    }


    @PostMapping("/add")
    public RestBean<String> addAdmin(@RequestParam String username,
                                     @RequestParam String password) {
        try {

            // 检查用户名是否已存在
            if (adminService.findByUsername(username) != null) {
                return RestBean.failure(400, "用户名已存在");
            }

            // 创建 Admin 对象
            Admin admin = new Admin();
            admin.setUsername(username);
            admin.setPassword(password);

            // 添加新管理员
            boolean success = adminService.addAdmin(admin);
            if (success) {
                return RestBean.success("管理员添加成功");
            } else {
                return RestBean.failure(500, "管理员添加失败");
            }
        } catch (Exception e) {
            logger.error("添加管理员失败", e);
            return RestBean.failure(500, "添加管理员失败: " + e.getMessage());
        }
    }


    @PostMapping("/delete")
    public RestBean<String> deleteAdmin(@RequestParam Integer id, HttpSession session) {
        try {
            Admin currentAdmin = (Admin) session.getAttribute("admin");
            // 防止删除自己
            if (currentAdmin != null && currentAdmin.getId().equals(id)) {
                return RestBean.failure(400, "不能删除当前登录的管理员");
            }

            boolean success = adminService.deleteAdmin(id);
            if (success) {
                return RestBean.success("管理员删除成功");
            } else {
                return RestBean.failure(500, "管理员删除失败");
            }
        } catch (Exception e) {
            logger.error("删除管理员失败", e);
            return RestBean.failure(500, "删除管理员失败");
        }
    }

    @PostMapping("/community/reply/delete")
    public RestBean<String> deleteReply(@RequestParam Integer replyId, HttpSession session) {
        try {
            Admin admin = (Admin) session.getAttribute("admin");
            if (admin == null) {
                return RestBean.failure(401, "请先登录管理员账户");
            }

            CommunityReply reply = communityReplyMapper.findWithReviewUserById(replyId);
            if (reply == null) {
                return RestBean.failure(404, "回复不存在");
            }

            communityReplyMapper.nullifyParentReplyId(replyId);
            int rows = communityReplyMapper.deleteReply(replyId);
            if (rows > 0) {
                return RestBean.success("删除成功");
            } else {
                return RestBean.failure(500, "删除失败");
            }
        } catch (Exception e) {
            logger.error("管理员删除回复失败", e);
            return RestBean.failure(500, "删除失败");
        }
    }
}