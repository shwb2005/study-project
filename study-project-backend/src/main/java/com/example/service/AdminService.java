package com.example.service;

import com.example.entity.user.Admin;

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