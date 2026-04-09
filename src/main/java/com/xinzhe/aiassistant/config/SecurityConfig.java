package com.xinzhe.aiassistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security配置类
 * 关闭默认登录页，只保留BCrypt密码加密功能
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 配置Security过滤规则，关闭默认登录页
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 关闭CSRF防护，前后端分离项目不需要
                .csrf(csrf -> csrf.disable())
                // 关闭Session，我们用JWT无状态认证
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 放行所有请求，认证逻辑我们自己用JWT拦截器实现
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                // 关闭默认的表单登录
                .formLogin(form -> form.disable())
                // 关闭默认的http基本认证
                .httpBasic(basic -> basic.disable());

        return http.build();
    }

    /**
     * 把BCrypt密码加密器交给Spring管理
     * 后续我们可以直接注入使用
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}