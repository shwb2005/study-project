package com.example.contorller;

import com.example.entity.Announcement;
import com.example.entity.RestBean;
import com.example.entity.user.AccountUser;
import com.example.entity.user.Admin;
import com.example.mapper.AnnouncementMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/announcement")
public class AnnouncementController {

    private static final Logger logger = LoggerFactory.getLogger(AnnouncementController.class);

    @Resource
    private AnnouncementMapper announcementMapper;

    // 用户端：获取公告列表（分页）
    @GetMapping("/list")
    public RestBean<Map<String, Object>> getList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "9") int pageSize) {
        try {
            int offset = (page - 1) * pageSize;
            List<Announcement> list = announcementMapper.findPaged(offset, pageSize);
            int total = announcementMapper.countAll();
            Map<String, Object> data = new HashMap<>();
            data.put("list", list);
            data.put("total", total);
            return RestBean.success(data);
        } catch (Exception e) {
            logger.error("获取公告列表失败", e);
            return RestBean.failure(500, (Map<String, Object>) null);
        }
    }

    // 用户端：获取全部公告（无分页）
    @GetMapping("/all")
    public RestBean<List<Announcement>> getAll() {
        try {
            return RestBean.success(announcementMapper.findAll());
        } catch (Exception e) {
            return RestBean.failure(500, List.of());
        }
    }

    // 管理员：发布公告
    @PostMapping("/admin/add")
    public RestBean<String> add(@RequestParam String title,
                                @RequestParam String content,
                                HttpSession session) {
        try {
            Admin admin = (Admin) session.getAttribute("admin");
            if (admin == null) {
                return RestBean.failure(401, "请先登录管理员账户");
            }
            Announcement a = new Announcement();
            a.setTitle(title);
            a.setContent(content);
            a.setAdminId(admin.getId());
            announcementMapper.insert(a);
            return RestBean.success("发布成功");
        } catch (Exception e) {
            logger.error("发布公告失败", e);
            return RestBean.failure(500, "发布失败");
        }
    }

    // 管理员：修改公告
    @PostMapping("/admin/update")
    public RestBean<String> update(@RequestParam Integer id,
                                   @RequestParam String title,
                                   @RequestParam String content,
                                   HttpSession session) {
        try {
            Admin admin = (Admin) session.getAttribute("admin");
            if (admin == null) {
                return RestBean.failure(401, "请先登录管理员账户");
            }
            Announcement a = new Announcement();
            a.setId(id);
            a.setTitle(title);
            a.setContent(content);
            announcementMapper.update(a);
            return RestBean.success("修改成功");
        } catch (Exception e) {
            logger.error("修改公告失败", e);
            return RestBean.failure(500, "修改失败");
        }
    }

    // 管理员：删除公告
    @PostMapping("/admin/delete")
    public RestBean<String> delete(@RequestParam Integer id, HttpSession session) {
        try {
            Admin admin = (Admin) session.getAttribute("admin");
            if (admin == null) {
                return RestBean.failure(401, "请先登录管理员账户");
            }
            announcementMapper.deleteById(id);
            return RestBean.success("删除成功");
        } catch (Exception e) {
            logger.error("删除公告失败", e);
            return RestBean.failure(500, "删除失败");
        }
    }
}
