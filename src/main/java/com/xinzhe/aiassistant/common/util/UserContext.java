package com.xinzhe.aiassistant.common.util;

/**
 * 用户上下文工具类
 * 基于ThreadLocal实现，在整个请求链路中传递当前登录用户的信息
 */
public class UserContext {

    // ThreadLocal对象，存储当前登录用户的ID
    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();

    /**
     * 设置当前用户ID
     * 拦截器里解析Token后调用
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前用户ID
     * 整个请求链路的任何地方都能调用
     */
    public static Long getCurrentUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 清除用户ID
     * 请求结束后必须调用，防止ThreadLocal内存泄漏
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
    }
}