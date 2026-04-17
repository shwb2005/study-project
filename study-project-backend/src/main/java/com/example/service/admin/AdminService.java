package com.example.service.admin;

import com.example.entity.admin.Admin;

import java.util.List;

public interface AdminService {

    boolean validateAdmin(String username, String password);

    Admin findByUsername(String username);
    // AdminService 接口中添加
    List<Admin> getAllAdmins();
    boolean addAdmin(Admin admin);
    boolean deleteAdmin(Integer id);
    boolean updateAdmin(Admin admin);
}