package com.example.mapper.announcement;

import com.example.entity.announcement.Announcement;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface AnnouncementMapper {

    @Select("SELECT * FROM announcement ORDER BY created_at DESC LIMIT #{offset}, #{pageSize}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "adminId", column = "admin_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<Announcement> findPaged(@Param("offset") int offset, @Param("pageSize") int pageSize);

    @Select("SELECT COUNT(*) FROM announcement")
    int countAll();

    @Select("SELECT * FROM announcement ORDER BY created_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "title", column = "title"),
            @Result(property = "content", column = "content"),
            @Result(property = "adminId", column = "admin_id"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<Announcement> findAll();

    @Insert("INSERT INTO announcement (title, content, admin_id, created_at) " +
            "VALUES (#{title}, #{content}, #{adminId}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Announcement announcement);

    @Update("UPDATE announcement SET title = #{title}, content = #{content} WHERE id = #{id}")
    int update(Announcement announcement);

    @Delete("DELETE FROM announcement WHERE id = #{id}")
    int deleteById(Integer id);
}
