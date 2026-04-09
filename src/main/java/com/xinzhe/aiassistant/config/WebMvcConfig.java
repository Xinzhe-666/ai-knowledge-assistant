package com.xinzhe.aiassistant.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC配置类
 * 配置拦截器、跨域等规则
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * 配置拦截器规则
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                // 放行以下公开接口，不需要登录就能访问
                .excludePathPatterns(
                        "/auth/register", // 用户注册
                        "/auth/login",    // 用户登录
                        "/test/**",       // 我们之前写的测试接口，全部放行
                        "/error"          // Spring Boot默认错误页面，必须放行
                );
    }
}