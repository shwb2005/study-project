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
        Admin admin = adminMapper.findAdminByUsername(username);
        if (admin != null) {
            return password.equals(admin.getPassword());
        }
        return false;
    }

    @Override
    public Admin findByUsername(String username) {
        return adminMapper.findAdminByUsername(username);
    }

    @Override
    public List<Admin> getAllAdmins() {
        return adminMapper.findAllAdmins();
    }

    @Override
    public boolean addAdmin(Admin admin) {
        Admin existingAdmin = adminMapper.findAdminByUsername(admin.getUsername());
        if (existingAdmin != null) {
            return false;
        }
        int result = adminMapper.insertAdmin(admin);
        return result > 0;
    }

    @Override
    public boolean deleteAdmin(Integer id) {
        int result = adminMapper.deleteAdminById(id);
        return result > 0;
    }

    @Override
    public boolean updateAdmin(Admin admin) {
        int result = adminMapper.updateAdmin(admin);
        return result > 0;
    }
}
