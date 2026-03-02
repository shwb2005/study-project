package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class StudyProjectBackendApplicationTests {

    @Autowired
    private JavaMailSender mailSender;

    public void sendTestMail() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("13736393394@163.com"); // 你的发件人邮箱
        message.setTo("你的收件人邮箱@example.com");
        message.setSubject("测试邮件");
        message.setText("这是一封测试邮件。");
        mailSender.send(message);
        System.out.println("邮件发送完成");
    }
}