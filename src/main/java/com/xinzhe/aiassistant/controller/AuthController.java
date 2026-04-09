package com.xinzhe.aiassistant.controller;

import com.xinzhe.aiassistant.common.result.Result;
import com.xinzhe.aiassistant.common.util.JwtUtil;
import com.xinzhe.aiassistant.common.util.UserContext;
import com.xinzhe.aiassistant.entity.User;
import com.xinzhe.aiassistant.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 用户认证接口（企业级安全版）
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * 用户注册接口（BCrypt加密版）
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody User user) {
        // 1. 校验用户名是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, user.getUsername());
        User existUser = userService.getOne(wrapper);
        if (existUser != null) {
            return Result.fail("用户名已存在");
        }

        // 2. 设置用户默认值，BCrypt加密密码
        user.setRole("user");
        user.setStatus(1);
        user.setDeleted(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        // 加密密码后再存储
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // 3. 保存用户到数据库
        boolean success = userService.save(user);
        if (success) {
            user.setPassword(null);
            return Result.success(user);
        } else {
            return Result.fail("注册失败");
        }
    }

    /**
     * 用户登录接口（BCrypt加密版）
     */
    @PostMapping("/login")
    public Result<String> login(@RequestBody User loginUser) {
        // 1. 根据用户名查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, loginUser.getUsername());
        User user = userService.getOne(wrapper);

        // 2. 校验用户是否存在
        if (user == null) {
            return Result.fail("用户名不存在");
        }

        // 3. BCrypt密码比对
        if (!passwordEncoder.matches(loginUser.getPassword(), user.getPassword())) {
            return Result.fail("密码错误");
        }

        // 4. 校验账号是否被锁定
        if (user.getStatus() == 0) {
            return Result.fail("账号已被锁定，请联系管理员");
        }

        // 5. 生成JWT Token
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        // 6. 返回Token
        return Result.success(token);
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/info")
    public Result<User> getUserInfo() {
        Long userId = UserContext.getCurrentUserId();
        User user = userService.getById(userId);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        user.setPassword(null);
        return Result.success(user);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        // 后续用Redis黑名单实现Token失效
        return Result.success();
    }
}