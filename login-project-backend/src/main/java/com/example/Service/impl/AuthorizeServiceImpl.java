package com.example.Service.impl;

import com.example.Service.AuthorizeService;
import com.example.entity.Account;
import com.example.mappeer.AccountMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class AuthorizeServiceImpl implements AuthorizeService {

    @Autowired
    AccountMapper accountMapper;
    @Autowired
    MailSender mailSender;
    @Value("${spring.mail.username}")
    String from;


    //通过Security框架对方法进行判断
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null)
            throw new UsernameNotFoundException("用户名不能为空");
        Account account = accountMapper.findAccountByNameOrEmail(username);
        if (account == null)
            throw new UsernameNotFoundException("用户名或密码错误");
        return User
                .withUsername(account.getUsername())
                .password(account.getPassword())
                .roles("user")
                .build();
    }

    //发送验证邮件
    public boolean sendValidateEmail(String email) {

        /**
         * 1.先生成对应的验证码
         * 2.把邮箱+SessionID+code放到Redis里面(过期时间为3分钟,剩余时间低于两分钟可以重新发送)
         * 3.发送验证码到指定邮箱
         * 4.如果发送失败的话,把Redis里面的刚刚插入的删除
         */
        Random random = new Random();
        int code = random.nextInt(899999) + 100000;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("您的验证邮件");
        message.setText("验证码是:" + code);
        try {
            mailSender.send(message);
            System.out.println("邮件发送成功!");
        } catch (MailException e) {
            e.printStackTrace();
        }
        return true;
    }
}
