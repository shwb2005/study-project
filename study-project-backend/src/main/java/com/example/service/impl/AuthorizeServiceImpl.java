package com.example.service.impl;

import com.example.entity.auth.Account;
import com.example.mapper.UserMapper;
import com.example.service.AuthorizeService;
import com.example.service.UserProfileService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthorizeServiceImpl implements AuthorizeService {

    @Value("${spring.mail.username}")
    String from;

    @Resource
    UserMapper mapper;

    @Resource // 新增：注入 UserProfileService
    private UserProfileService userProfileService;

    @Resource
    MailSender mailSender;

    @Resource
    StringRedisTemplate template;


    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if(username == null || "".equals(username)) {
            throw new UsernameNotFoundException("用户名不能为空");
        }
        Account account = mapper.findAccountByUsername(username);
        if(account == null) {
            throw new UsernameNotFoundException("用户名或密码错误");
        }
        return User
                .withUsername(account.getUsername())
                .password(account.getPassword())
                .roles("user")
                .build();
    }

    /**
     * 1生成验证码
     * 2把邮箱和对应的验证码放到Redis里（过期时间3分钟，如果此时重新要求发邮件，那么，只要剩余时间低于2分钟，就可以再发一次，重复此流程）
     * 3发送验证码到指定邮箱
     * 4如果发送失败，把Redis里刚刚插入的数据删除
     * 5用户在注册时，再从Redis里取出对应的值，然后验证是否一致
     */

    @Override
    public String sendValidateEmail(String email, String sessionId,boolean hasAccount) {
        String key = "email:" +sessionId + ":" +email+":"+hasAccount;
        if(Boolean.TRUE.equals(template.hasKey(key))) {
            Long expire = Optional.ofNullable(template.getExpire(key, TimeUnit.SECONDS)).orElse(0L);
            if(expire > 120)
                return "请求频繁，请稍后再试";
        }
        Account account = mapper.findAccountByUsername(email);
        if(hasAccount && account == null)
            return "没有此邮件地址账户";
        if(!hasAccount && account != null)
            return "此邮箱已被注册";
        Random random = new Random();
        int code = random.nextInt(900000)+100000;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("您的验证邮件");
        message.setText("验证码是 "+code);
        try{
            mailSender.send(message);
            template.opsForValue().set(key,String.valueOf(code),3, TimeUnit.MINUTES);
            return null;
        }catch (MailException e){
            e.printStackTrace();
            return "邮件发送失败";
        }

    }

    @Override
    @Transactional
    public String validateAndRegister(String username, String password, String email, String code, String sessionId){
        String key = "email:" + sessionId + ":" + email + ":false";
        if(Boolean.TRUE.equals(template.hasKey(key))) {
            String s = template.opsForValue().get(key);
            if(s == null) return "验证码失败，请重新请求";
            if(s.equals(code)){
                password = encoder.encode(password);

                try {
                    // 创建 Account 对象
                    Account account = new Account();
                    account.setUsername(username);
                    account.setPassword(password);
                    account.setEmail(email);

                    // 插入用户账号
                    int result = mapper.createAccount(account);

                    if(result > 0){
                        Integer newUserId = account.getId();
                        if (newUserId != null) {
                            userProfileService.initializeUserProfile(newUserId);
                            template.delete(key);
                            return null;
                        } else {
                            return "注册失败，无法获取用户ID";
                        }
                    } else {
                        return "内部错误，联系管理员";
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return "注册失败，请联系管理员";
                }
            } else {
                return "验证码错误";
            }
        } else {
            return "请先获取验证码";
        }
    }
    @Override
    public String validateOnly(String email, String code, String sessionId) {
        String key = "email:" +sessionId + ":" +email+":true";
        if(Boolean.TRUE.equals(template.hasKey(key))) {
            String s = template.opsForValue().get(key);
            if(s == null ) return "验证码失败，请重新请求";
            if(s.equals(code)){
              return null;
            }else{
                return "验证码错误";
            }
        }else{
            return "请求获取验证码";
        }
    }

    @Override
    public boolean resetPassword(String password, String email) {
        password = encoder.encode(password);
        return mapper.resetPassword(password,email)>0;
    }

}
