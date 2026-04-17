package com.example.mapper.community;

import com.example.entity.community.CommunityReviewLike;
import org.apache.ibatis.annotations.*;
import java.util.List;

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

    @Select("<script>" +
            "SELECT * FROM community_review_likes WHERE user_id = #{userId} AND review_id IN " +
            "<foreach item='item' index='index' collection='reviewIds' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "reviewId", column = "review_id"),
            @Result(property = "type", column = "type"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<CommunityReviewLike> findByUserAndReviewIds(@Param("userId") Integer userId, @Param("reviewIds") List<Integer> reviewIds);
}
