package com.example.mapper;

import com.example.entity.user.Admin;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AdminMapper {

    @Select("SELECT * FROM admin_users WHERE username = #{username}")
    Admin findAdminByUsername(String username);
    @Select("SELECT id, username, role FROM admin_users ORDER BY id")
    List<Admin> findAllAdmins();

    @Insert("INSERT INTO admin_users (username, password, role) VALUES (#{username}, #{password}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertAdmin(Admin admin);

    @Delete("DELETE FROM admin_users WHERE id = #{id}")
    int deleteAdminById(Integer id);

    @Update("UPDATE admin_users SET role = #{role} WHERE id = #{id}")
    int updateAdmin(Admin admin);
}