package com.example.mapper;

import com.example.entity.CommunityReply;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CommunityReplyMapper {

    @Select("SELECT r.*, " +
            "CASE WHEN r.user_id = cr.user_id AND cr.is_anonymous = 0 THEN 1 ELSE 0 END as is_author_reply, " +
            "CASE WHEN #{currentUserId} IS NOT NULL AND (r.user_id = #{currentUserId} OR cr.user_id = #{currentUserId}) THEN 1 ELSE 0 END as can_delete " +
            "FROM community_replies r " +
            "LEFT JOIN community_reviews cr ON r.review_id = cr.id " +
            "WHERE r.review_id = #{reviewId} " +
            "ORDER BY r.created_at ASC")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "reviewId", column = "review_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "username", column = "username"),
            @Result(property = "avatar", column = "avatar"),
            @Result(property = "content", column = "content"),
            @Result(property = "isAnonymous", column = "is_anonymous"),
            @Result(property = "isAuthorReply", column = "is_author_reply"),
            @Result(property = "canDelete", column = "can_delete"),
            @Result(property = "likeCount", column = "like_count"),
            @Result(property = "dislikeCount", column = "dislike_count"),
            @Result(property = "parentReplyId", column = "parent_reply_id"),
            @Result(property = "replyToUserId", column = "reply_to_user_id"),
            @Result(property = "replyToUsername", column = "reply_to_username"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<CommunityReply> findByReviewId(@Param("reviewId") Integer reviewId, @Param("currentUserId") Integer currentUserId);

    @Insert("INSERT INTO community_replies (review_id, user_id, username, avatar, content, is_anonymous, parent_reply_id, reply_to_user_id, reply_to_username, like_count, dislike_count, created_at) " +
            "VALUES (#{reviewId}, #{userId}, #{username}, #{avatar}, #{content}, #{isAnonymous}, #{parentReplyId}, #{replyToUserId}, #{replyToUsername}, 0, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CommunityReply reply);

    @Update("UPDATE community_replies SET like_count = like_count + #{delta} WHERE id = #{replyId}")
    int updateLikeCount(@Param("replyId") Integer replyId, @Param("delta") int delta);

    @Update("UPDATE community_replies SET dislike_count = dislike_count + #{delta} WHERE id = #{replyId}")
    int updateDislikeCount(@Param("replyId") Integer replyId, @Param("delta") int delta);

    @Update("UPDATE community_replies SET parent_reply_id = NULL WHERE parent_reply_id = #{replyId}")
    int nullifyParentReplyId(@Param("replyId") Integer replyId);

    @Select("SELECT r.*, cr.user_id as review_user_id " +
            "FROM community_replies r " +
            "LEFT JOIN community_reviews cr ON r.review_id = cr.id " +
            "WHERE r.id = #{replyId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "reviewId", column = "review_id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "reviewUserId", column = "review_user_id")
    })
    CommunityReply findWithReviewUserById(@Param("replyId") Integer replyId);

    @Delete("DELETE FROM community_replies WHERE id = #{replyId}")
    int deleteReply(@Param("replyId") Integer replyId);
}
