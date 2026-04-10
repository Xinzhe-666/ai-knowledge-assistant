package com.xinzhe.aiassistant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinzhe.aiassistant.common.result.Result;
import com.xinzhe.aiassistant.common.util.UserContext;
import com.xinzhe.aiassistant.entity.ChatMessage;
import com.xinzhe.aiassistant.entity.ChatSession;
import com.xinzhe.aiassistant.service.ChatMessageService;
import com.xinzhe.aiassistant.service.ChatSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息接口
 * 所有接口都需要登录
 */
@RestController
@RequestMapping("/message")
public class ChatMessageController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private ChatSessionService chatSessionService;

    /**
     * 查询某个会话的所有聊天消息
     * 接口地址：GET /message/list/{sessionId}
     */
    @GetMapping("/list/{sessionId}")
    public Result<List<ChatMessage>> getMessageBySessionId(@PathVariable Long sessionId) {
        Long userId = UserContext.getCurrentUserId();

        // 先校验会话权限，防止越权访问别人的消息
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return Result.fail("会话不存在或无权限访问");
        }

        // 查询会话里的所有未删除消息，按创建时间正序（最早的消息排在前面）
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getDeleted, 0)
                .orderByAsc(ChatMessage::getCreatedAt);

        List<ChatMessage> messageList = chatMessageService.list(wrapper);
        return Result.success(messageList);
    }

    /**
     * 删除单条消息（逻辑删除）
     * 接口地址：DELETE /message/delete/{messageId}
     */
    @DeleteMapping("/delete/{messageId}")
    public Result<Void> deleteMessage(@PathVariable Long messageId) {
        Long userId = UserContext.getCurrentUserId();

        // 校验权限：先查消息，再查所属会话的userId是否匹配
        ChatMessage message = chatMessageService.getById(messageId);
        if (message == null) {
            return Result.fail("消息不存在");
        }
        ChatSession session = chatSessionService.getById(message.getSessionId());
        if (session == null || !session.getUserId().equals(userId)) {
            return Result.fail("无权限删除该消息");
        }

        boolean success = chatMessageService.removeById(messageId);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("删除消息失败");
        }
    }

    /**
     * 清空某个会话的所有消息
     * 接口地址：DELETE /message/clear/{sessionId}
     */
    @DeleteMapping("/clear/{sessionId}")
    public Result<Void> clearSessionMessage(@PathVariable Long sessionId) {
        Long userId = UserContext.getCurrentUserId();

        // 校验权限
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return Result.fail("会话不存在或无权限操作");
        }

        // 逻辑删除该会话的所有消息
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId);
        boolean success = chatMessageService.remove(wrapper);
        if (success) {
            return Result.success();
        } else {
            return Result.fail("清空消息失败");
        }
    }
}