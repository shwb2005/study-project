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
            @Result(property = "type", column = "type"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "dislikeCount", column = "dislike_count"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<CommunityReview> findAll();

    @Select("SELECT * FROM community_reviews WHERE type = 0 OR type IS NULL ORDER BY updated_at DESC")
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
            @Result(property = "type", column = "type"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "dislikeCount", column = "dislike_count"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<CommunityReview> findReviews();

    @Select("SELECT * FROM community_reviews WHERE type = 1 ORDER BY updated_at DESC")
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
            @Result(property = "type", column = "type"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "dislikeCount", column = "dislike_count"),
            @Result(property = "createdAt", column = "created_at"),
            @Result(property = "updatedAt", column = "updated_at")
    })
    List<CommunityReview> findDiscussions();

    @Insert("INSERT INTO community_reviews (user_id, username, avatar, course_id, course_name, type, rating, review, is_anonymous, created_at, updated_at) " +
            "VALUES (#{userId}, #{username}, #{avatar}, #{courseId}, #{courseName}, #{type}, #{rating}, #{review}, #{isAnonymous}, NOW(), NOW()) " +
            "ON DUPLICATE KEY UPDATE " +
            "rating = VALUES(rating), review = VALUES(review), type = VALUES(type), " +
            "username = VALUES(username), avatar = VALUES(avatar), " +
            "course_name = VALUES(course_name), is_anonymous = VALUES(is_anonymous), updated_at = NOW()")
    int insertOrUpdate(CommunityReview review);

    @Insert("INSERT INTO community_reviews (user_id, username, avatar, course_id, course_name, type, rating, review, is_anonymous, created_at, updated_at) " +
            "VALUES (#{userId}, #{username}, #{avatar}, #{courseId}, #{courseName}, 1, 0, #{review}, #{isAnonymous}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insertDiscussion(CommunityReview discussion);

    @Update("UPDATE community_reviews SET like_count = like_count + #{delta} WHERE id = #{reviewId}")
    int updateLikeCount(@Param("reviewId") Integer reviewId, @Param("delta") int delta);

    @Update("UPDATE community_reviews SET dislike_count = dislike_count + #{delta} WHERE id = #{reviewId}")
    int updateDislikeCount(@Param("reviewId") Integer reviewId, @Param("delta") int delta);

    @Delete("DELETE FROM community_reviews WHERE id = #{reviewId}")
    int deleteReview(@Param("reviewId") Integer reviewId);
}
