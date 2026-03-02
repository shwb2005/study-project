package com.example.contorller;

import com.example.entity.RestBean;
import com.example.entity.user.AccountUser;
import com.example.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    UserMapper userMapper;

    @GetMapping("/me")
    public RestBean<AccountUser> me(@SessionAttribute(value = "account", required = false) AccountUser user) {
        if (user == null) {
            // 直接创建 RestBean 实例，避免泛型问题
            return new RestBean<>(401, false, null);
        }
        return new RestBean<>(200, true, user);
    }

    // 新增接口：获取账户基本信息（用户名和邮箱）
    @GetMapping("/account-info")
    public RestBean<Map<String, String>> getAccountInfo(@SessionAttribute(value = "account", required = false) AccountUser user) {
        if (user == null) {
            // 对于 Map 类型的返回，也需要保持一致
            return new RestBean<>(401, false, null);
        }

        Map<String, String> accountInfo = new HashMap<>();
        accountInfo.put("username", user.getUsername());
        accountInfo.put("email", user.getEmail());

        return new RestBean<>(200, true, accountInfo);
    }


}