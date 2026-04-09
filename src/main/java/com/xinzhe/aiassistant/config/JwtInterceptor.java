package com.xinzhe.aiassistant.config;

import com.xinzhe.aiassistant.common.exception.BusinessException;
import com.xinzhe.aiassistant.common.util.JwtUtil;
import com.xinzhe.aiassistant.common.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT身份认证拦截器
 * 在接口执行前校验Token，实现身份认证
 */
@Component
@Slf4j
public class JwtInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 接口执行前执行：核心认证逻辑
     * @return true=放行，false=拦截
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. 从请求头里获取Token
        // 前端请求的规范：请求头里的key是Authorization，value是Bearer 你的Token
        String authHeader = request.getHeader("Authorization");

        // 2. 判断请求头里有没有Token
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            log.warn("请求未携带Token，请求地址：{}", request.getRequestURI());
            throw new BusinessException(401, "请先登录");
        }

        // 3. 提取Token：去掉前面的"Bearer "前缀
        String token = authHeader.substring(7);

        // 4. 校验Token是否有效
        if (!jwtUtil.validateToken(token)) {
            log.warn("无效的Token，请求地址：{}", request.getRequestURI());
            throw new BusinessException(401, "Token无效或已过期，请重新登录");
        }

        // 5. 解析Token，获取用户ID
        Long userId = jwtUtil.getUserIdFromToken(token);
        log.info("用户登录成功，用户ID：{}，请求地址：{}", userId, request.getRequestURI());

        // 6. 把用户ID放到UserContext里，整个请求链路都能取到
        UserContext.setUserId(userId);

        // 7. 放行请求，进入Controller
        return true;
    }

    /**
     * 请求完全结束后执行：清除ThreadLocal，防止内存泄漏
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }
}