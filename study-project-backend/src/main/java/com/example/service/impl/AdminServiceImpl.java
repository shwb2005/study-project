package com.example.service.impl;

import com.example.entity.user.Admin;
import com.example.mapper.AdminMapper;
import com.example.service.AdminService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminServiceImpl implements AdminService {

    @Resource
    private AdminMapper adminMapper;

    @Override
    public boolean validateAdmin(String username, String password) {
        System.out.println("=== 开始验证管理员（明文） ===");
        System.out.println("输入用户名: " + username);
        System.out.println("输入密码: " + password);

        Admin admin = adminMapper.findAdminByUsername(username);
        System.out.println("数据库查询结果: " + admin);

        if (admin != null) {
            System.out.println("数据库中的密码: " + admin.getPassword());
            // 直接比较明文密码
            boolean matches = password.equals(admin.getPassword());
            System.out.println("密码匹配结果: " + matches);
            return matches;
        } else {
            System.out.println("未找到管理员用户: " + username);
            return false;
        }
    }

    @Override
    public Admin findByUsername(String username) {
        return adminMapper.findAdminByUsername(username);
    }

    @Override
    public List<Admin> getAllAdmins() {
        System.out.println("=== 获取所有管理员列表 ===");
        List<Admin> admins = adminMapper.findAllAdmins();
        System.out.println("查询到的管理员数量: " + (admins != null ? admins.size() : 0));
        return admins;
    }

    @Override
    public boolean addAdmin(Admin admin) {
        System.out.println("=== 添加新管理员 ===");
        System.out.println("新管理员信息 - 用户名: " + admin.getUsername() + ", 密码: " + admin.getPassword());

        try {
            // 检查用户名是否已存在
            Admin existingAdmin = adminMapper.findAdminByUsername(admin.getUsername());
            if (existingAdmin != null) {
                System.out.println("用户名已存在: " + admin.getUsername());
                return false;
            }

            // 插入新管理员
            int result = adminMapper.insertAdmin(admin);
            boolean success = result > 0;
            System.out.println("添加管理员结果: " + (success ? "成功" : "失败"));
            return success;
        } catch (Exception e) {
            System.out.println("添加管理员异常: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteAdmin(Integer id) {
        System.out.println("=== 删除管理员 ===");
        System.out.println("要删除的管理员ID: " + id);

        try {
            int result = adminMapper.deleteAdminById(id);
            boolean success = result > 0;
            System.out.println("删除管理员结果: " + (success ? "成功" : "失败"));
            return success;
        } catch (Exception e) {
            System.out.println("删除管理员异常: " + e.getMessage());
            return false;
        }
    }
}