package com.xinzhe.aiassistant.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC配置类
 * 配置拦截器、跨域等规则
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Resource
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                // 拦截所有请求
                .addPathPatterns("/**")
                // 放行无需登录的接口
                .excludePathPatterns(
                        // 登录/注册接口
                        "/auth/login",
                        "/auth/register",

                        // 兼容如果以后加了 /api 前缀
                        "/api/auth/login",
                        "/api/auth/register",

                        // Sentinel / Actuator
                        "/registry/**",
                        "/actuator/**",

                        // Swagger / OpenAPI
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**",
                        "/v2/api-docs/**",
                        "/webjars/**",

                        // 其他
                        "/error",
                        "/favicon.ico",
                        "/"
                );
    }
}