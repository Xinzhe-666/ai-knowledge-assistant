package com.xinzhe.aiassistant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinzhe.aiassistant.common.result.Result;
import com.xinzhe.aiassistant.common.util.UserContext;
import com.xinzhe.aiassistant.entity.ChatSession;
import com.xinzhe.aiassistant.service.ChatSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 会话管理接口
 * 所有接口都需要登录，带Token才能访问
 */
@RestController
@RequestMapping("/session")
public class ChatSessionController {

    @Autowired
    private ChatSessionService chatSessionService;

    /**
     * 1. 创建新会话
     * 接口地址：POST /session/create
     */
    @PostMapping("/create")
    public Result<ChatSession> createSession(@RequestBody ChatSession session) {
        // 从UserContext获取当前登录用户ID，绝对不能用前端传的userId，防止越权
        Long userId = UserContext.getCurrentUserId();

        // 设置会话所属用户
        session.setUserId(userId);
        // 设置默认会话名称，如果前端没传
        if (session.getSessionName() == null || session.getSessionName().isEmpty()) {
            session.setSessionName("新对话 " + LocalDateTime.now().toString().substring(0, 16));
        }
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setDeleted(0);

        // 保存到数据库
        boolean success = chatSessionService.save(session);
        if (success) {
            return Result.success(session);
        } else {
            return Result.fail("创建会话失败");
        }
    }

    /**
     * 2. 查询当前用户的所有会话
     * 接口地址：GET /session/list
     */
    @GetMapping("/list")
    public Result<List<ChatSession>> getMySessions() {
        Long userId = UserContext.getCurrentUserId();

        // 查询当前用户的所有未删除会话，按更新时间倒序（最近聊的排在前面）
        LambdaQueryWrapper<ChatSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getDeleted, 0)
                .orderByDesc(ChatSession::getUpdatedAt);

        List<ChatSession> sessionList = chatSessionService.list(wrapper);
        return Result.success(sessionList);
    }

    /**
     * 3. 根据ID查询会话详情
     * 接口地址：GET /session/get/{sessionId}
     */
    @GetMapping("/get/{sessionId}")
    public Result<ChatSession> getSessionById(@PathVariable Long sessionId) {
        Long userId = UserContext.getCurrentUserId();

        // 查询会话
        ChatSession session = chatSessionService.getById(sessionId);
        // 权限校验：会话不属于当前用户，直接返回错误，防止越权访问
        if (session == null || !session.getUserId().equals(userId)) {
            return Result.fail("会话不存在或无权限访问");
        }
        // 过滤已删除的会话
        if (session.getDeleted() == 1) {
            return Result.fail("会话已被删除");
        }

        return Result.success(session);
    }

    /**
     * 4. 修改会话名称
     * 接口地址：PUT /session/update
     */
    @PutMapping("/update")
    public Result<ChatSession> updateSessionName(@RequestBody ChatSession session) {
        Long userId = UserContext.getCurrentUserId();

        // 先查询会话，校验权限
        ChatSession existSession = chatSessionService.getById(session.getId());
        if (existSession == null || !existSession.getUserId().equals(userId)) {
            return Result.fail("会话不存在或无权限修改");
        }

        // 只允许修改会话名称
        existSession.setSessionName(session.getSessionName());
        existSession.setUpdatedAt(LocalDateTime.now());

        boolean success = chatSessionService.updateById(existSession);
        if (success) {
            return Result.success(existSession);
        } else {
            return Result.fail("修改会话失败");
        }
    }

    /**
     * 5. 删除会话（逻辑删除）
     * 接口地址：DELETE /session/delete/{sessionId}
     */
    @DeleteMapping("/delete/{sessionId}")
    public Result<Void> deleteSession(@PathVariable Long sessionId) {
        Long userId = UserContext.getCurrentUserId();

        // 校验权限
        ChatSession existSession = chatSessionService.getById(sessionId);
        if (existSession == null || !existSession.getUserId().equals(userId)) {
            return Result.fail("会话不存在或无权限删除");
        }

        // 逻辑删除，MyBatis-Plus会自动更新deleted字段为1
        boolean success = chatSessionService.removeById(sessionId);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("删除会话失败");
        }
    }
}