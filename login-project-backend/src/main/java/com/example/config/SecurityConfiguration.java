package com.example.config;

import com.alibaba.fastjson.JSONObject;
import com.example.Service.AuthorizeService;
import com.example.entity.RestBean;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

//SpringBoot3.0不需要继承WebSecurityAdapter,那个已经过时了
//现在使用注解就可以
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Autowired
    AuthorizeService authorizeService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                //请求拦截
                .authorizeHttpRequests().anyRequest().authenticated()
                .and()
                //表单登录接口
                .formLogin().loginProcessingUrl("/api/auth/login")
                //设置登陆成功后返回信息
                .successHandler(this::onAuthenticationSuccess)
                //登录失败返回信息
                .failureHandler(this::onAuthenticationFailure)
                .and()
                //登出接口
                .logout().logoutUrl("/api/auth/logout")
                .and()
                //关闭csrf
                .csrf().disable()
                //异常信息返回
                .exceptionHandling().authenticationEntryPoint(this::onAuthenticationFailure)
                .and()
                .build();
    }

    //自定义用户登录验证Service
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity security) throws Exception{
        return security
                .getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(authorizeService)
                .and()
                .build();
    }

    //密码生成策略
    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    //自定义登录成功返回信息
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(JSONObject.toJSONString(RestBean.success("登陆成功")));
    }

    //登录失败返回信息
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setCharacterEncoding("utf-8");
        response.getWriter().write(JSONObject.toJSONString(RestBean.failure(401,exception.getMessage())));
    }
}
