package com.example.mapper;

import com.example.entity.auth.Account;
import com.example.entity.user.AccountUser;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    @Select("select * from db_account where username = #{text} or email = #{text}")
    Account findAccountByUsername(String text);

    @Select("select * from db_account where username = #{text} or email = #{text}")
    AccountUser findAccountUserByNameOrEmail(String text);

    // 关键修改：添加 @Options 注解返回生成的主键
    @Insert("insert into db_account(username, password, email) values (#{username},#{password},#{email})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int createAccount(Account account);  // 改为接收 Account 对象

    @Update("update db_account set password = #{password} where email = #{email}")
    int resetPassword(String password, String email);

    // 修复：去掉不存在的 points 和 study_time 字段
    @Select("SELECT a.*, p.real_name, p.avatar, p.phone, p.gender, p.birthday, p.bio, " +
            "p.last_login_time " +  // 移除了 p.points, p.study_time
            "FROM db_account a " +
            "LEFT JOIN user_profile p ON a.id = p.user_id " +
            "WHERE a.id = #{id}")
    AccountUser findWithProfileById(Integer id);
}