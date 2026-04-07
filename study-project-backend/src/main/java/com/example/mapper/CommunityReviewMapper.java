package com.example.mapper;

import com.example.entity.CommunityReview;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommunityReviewMapper {

    @Select("SELECT * FROM community_reviews ORDER BY updated_at DESC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "avatar", column = "avatar"),
            @Result(property = "courseId", column = "course_id"),
            @Result(property = "courseName", column = "course_name"),
            @Result(property = "rating", column = "rating"),
            @Result(property = "review", column = "review"),
            @Result(property = "isAnonymous", column = "is_anonymous"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<CommunityReview> findAll();

    @Insert("INSERT INTO community_reviews (user_id, username, avatar, course_id, course_name, rating, review, is_anonymous, created_at, updated_at) " +
            "VALUES (#{userId}, #{username}, #{avatar}, #{courseId}, #{courseName}, #{rating}, #{review}, #{isAnonymous}, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "rating = VALUES(rating), review = VALUES(review), " +
            "username = VALUES(username), avatar = VALUES(avatar), " +
            "course_name = VALUES(course_name), is_anonymous = VALUES(is_anonymous), updated_at = NOW()")
    int insertOrUpdate(CommunityReview review);
}
