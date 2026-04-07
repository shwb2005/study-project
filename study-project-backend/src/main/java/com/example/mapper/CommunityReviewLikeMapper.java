package com.example.mapper;

import com.example.entity.CommunityReviewLike;
import org.apache.ibatis.annotations.*;

@Mapper
public interface CommunityReviewLikeMapper {

    @Select("SELECT * FROM community_review_likes WHERE user_id = #{userId} AND review_id = #{reviewId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "reviewId", column = "review_id"),
            @Result(property = "type", column = "type"),
            @Result(property = "createdAt", column = "created_at")
    })
    CommunityReviewLike findByUserAndReview(@Param("userId") Integer userId, @Param("reviewId") Integer reviewId);

    @Insert("INSERT INTO community_review_likes (user_id, review_id, type, created_at) " +
            "VALUES (#{userId}, #{reviewId}, #{type}, NOW()) " +
            "ON DUPLICATE KEY UPDATE type = VALUES(type), created_at = NOW()")
    int insertOrUpdate(CommunityReviewLike like);

    @Delete("DELETE FROM community_review_likes WHERE user_id = #{userId} AND review_id = #{reviewId}")
    int deleteByUserAndReview(@Param("userId") Integer userId, @Param("reviewId") Integer reviewId);
}
