package com.example.mapper;

import com.example.entity.CommunityReplyLike;
import org.apache.ibatis.annotations.*;
import java.util.List;

@Mapper
public interface CommunityReplyLikeMapper {

    @Select("SELECT * FROM community_reply_likes WHERE user_id = #{userId} AND reply_id = #{replyId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "replyId", column = "reply_id"),
            @Result(property = "type", column = "type"),
            @Result(property = "createdAt", column = "created_at")
    })
    CommunityReplyLike findByUserAndReply(@Param("userId") Integer userId, @Param("replyId") Integer replyId);

    @Insert("INSERT INTO community_reply_likes (user_id, reply_id, type, created_at) " +
            "VALUES (#{userId}, #{replyId}, #{type}, NOW()) " +
            "ON DUPLICATE KEY UPDATE type = VALUES(type), created_at = NOW()")
    int insertOrUpdate(CommunityReplyLike like);

    @Delete("DELETE FROM community_reply_likes WHERE user_id = #{userId} AND reply_id = #{replyId}")
    int deleteByUserAndReply(@Param("userId") Integer userId, @Param("replyId") Integer replyId);

    @Select("<script>" +
            "SELECT * FROM community_reply_likes WHERE user_id = #{userId} AND reply_id IN " +
            "<foreach item='item' index='index' collection='replyIds' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            "</script>")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "replyId", column = "reply_id"),
            @Result(property = "type", column = "type"),
            @Result(property = "createdAt", column = "created_at")
    })
    List<CommunityReplyLike> findByUserAndReplyIds(@Param("userId") Integer userId, @Param("replyIds") List<Integer> replyIds);
}
