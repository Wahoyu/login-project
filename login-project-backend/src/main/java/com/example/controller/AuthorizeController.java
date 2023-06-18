package com.example.controller;

import com.example.Service.AuthorizeService;
import com.example.entity.RestBean;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthorizeController {

    private final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+.[a-zA-Z]{2,}$";
    private final String USERNAME_REGEX = "^[a-zA-Z0-9\u4e00-\u9fa5]+$";

    @Resource
    AuthorizeService service;

    //注册时发送验证码
    @PostMapping("/valid-register-email")
    public RestBean<String> validateRegisterEmail(@Pattern(regexp = EMAIL_REGEX) @RequestParam("email") String email,
                                          HttpSession session) {
        String s = service.sendValidateEmail(email, session.getId(),false);
        if (s == null)
            return RestBean.success("验证码已发送");
        else
            return RestBean.failure(400, s);
    }

    //重置密码时发送验证码
    @PostMapping("/valid-reset-email")
    public RestBean<String> validateResetEmail(@Pattern(regexp = EMAIL_REGEX) @RequestParam("email") String email,
                                          HttpSession session) {
        String s = service.sendValidateEmail(email, session.getId(),true);
        if (s == null)
            return RestBean.success("验证码已发送");
        else
            return RestBean.failure(400, s);
    }

    //注册逻辑
    @PostMapping("/register")
    public RestBean<String> registerUser(@Pattern(regexp = USERNAME_REGEX) @Length(min = 2, max = 8) @RequestParam("username") String username,
                                         @Length(min = 6, max = 16) @RequestParam("password") String password,
                                         @Pattern(regexp = EMAIL_REGEX) @RequestParam("email") String email,
                                         @Length(min = 6, max = 6) @RequestParam("code") String code,
                                         HttpSession session) {

        String s = service.validateAndRegister(username, password, email, code, session.getId());
        if (s == null) {
            return RestBean.success("您已注册成功!");
        } else {
            return RestBean.failure(400, s);
        }
    }

    //重置密码时,对验证码进行校验
    /**
     * 1. 发送验证码邮件
     * 2. 对验证码进行验证,如果正确就在Session中存一个标记
     * 3. 用户发起重置密码请求,如果存在标记就成功重置
     */
    @PostMapping("/start-reset")
    public RestBean<String> startReset(@Pattern(regexp = EMAIL_REGEX) @RequestParam("email") String email,
                                       @Length(min = 6, max = 6) @RequestParam("code") String code,
                                       HttpSession session) {
        String s = service.validateOnly(email, code, session.getId());
        if (s == null) {
            session.setAttribute("reset-password", email);
            return RestBean.success();
        }else{
            return RestBean.failure(400, s);
        }
    }

    //重置密码逻辑
    @PostMapping("/do-reset")
    public RestBean<String> resetPassword(@Length(min = 6, max = 16) @RequestParam("password") String password,
                                          HttpSession session) {
        String email = (String) session.getAttribute("reset-password");
        if (email == null) {
            return RestBean.failure(401, "请先完成邮件验证");
        } else if (service.resetPassword(password, email)) {
            session.removeAttribute("reset-password");
            return RestBean.success("密码重置成功");
        }else{
            return RestBean.failure(500, "内部错误");
        }
    }


}
