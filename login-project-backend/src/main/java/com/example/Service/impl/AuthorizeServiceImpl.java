package com.example.Service.impl;

import com.example.Service.AuthorizeService;
import com.example.entity.Account;
import com.example.mappeer.AccountMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
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

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class AuthorizeServiceImpl implements AuthorizeService {

    @Autowired
    AccountMapper accountMapper;
    @Autowired
    MailSender mailSender;
    @Value("${spring.mail.username}")
    String from;
    @Resource
    StringRedisTemplate template;
    BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();


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

    //发送验证码
    public String sendValidateEmail(String email, String sessionId, boolean hasAccount) {

        /**
         * 1.先生成对应的验证码
         * 2.把邮箱+SessionID+code放到Redis里面(过期时间为3分钟,剩余时间低于两分钟可以重新发送)
         * 3.发送验证码到指定邮箱
         * 4.如果发送失败的话,把Redis里面的刚刚插入的删除
         */

        String key = "email:" + sessionId + ":" + email + ":" + hasAccount;
        //System.out.println("我们生成的key是"+key);
        if (Boolean.TRUE.equals(template.hasKey(key))) {
            Long expire = Optional.ofNullable(template.getExpire(key, TimeUnit.SECONDS)).orElse(0L);
            if (expire > 120) return "请求频繁,请稍后再试";
        }

        Account account = accountMapper.findAccountByNameOrEmail(email);
        if (hasAccount && account == null) return "此邮箱还没有被注册!";
        if (!hasAccount && account != null) return "该邮箱已注册!";


        Random random = new Random();
        int code = random.nextInt(899999) + 100000;
        System.out.println("验证码:" + code);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(email);
        message.setSubject("您的验证邮件");
        message.setText("验证码是:" + code);
        try {
            mailSender.send(message);
            template.opsForValue().set(key, String.valueOf(code), 3, TimeUnit.MINUTES);
            return null;
        } catch (MailException e) {
            e.printStackTrace();
            return "邮件发送失败";
        }
    }

    //对验证码进行校验并向数据库中写入用户
    @Override
    public String validateAndRegister(String username, String password, String email, String code, String sessionId) {

        String key = "email:" + sessionId + ":" + email + ":false";
        //System.out.println("验证时生成的key是"+key);
        boolean b = Boolean.TRUE.equals(template.hasKey(key));
        //System.out.println(b);
        if (b) {
            String s = template.opsForValue().get(key);
            if (s == null) return "验证码已失效";
            if (s.equals(code)) {
                Account account = accountMapper.findAccountByNameOrEmail(username);
                if(account != null) return "此用户名已经被注册,请更换用户名";
                template.delete(key);
                password = encoder.encode(password);
                if (accountMapper.createAccount(username, password, email) > 0) {
                    return null;
                } else {
                    return "请更换用户名进行尝试";
                }
            } else {
                return "验证码错误";
            }
        } else {
            //System.out.println("没找到key");
            return "验证码校验失败";
        }
    }

    @Override
    public String validateOnly(String email, String code, String sessionId) {
        String key = "email:" + sessionId + ":" + email + ":true";

        if (Boolean.TRUE.equals(template.hasKey(key))) {
            String s = template.opsForValue().get(key);
            if (s == null) return "验证码失败,请重新登陆";
            if (s.equals(code)) {
                template.delete(key);
                return null;
            } else {
                return "验证码错误";
            }
        } else {
            return "验证码校验失败";
        }
    }

    @Override
    public boolean resetPassword(String password, String email) {
        password = encoder.encode(password);
        return accountMapper.resetPasswordByEmail(password, email) > 0;
    }
}
