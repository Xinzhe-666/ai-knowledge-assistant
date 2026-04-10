package com.xinzhe.aiassistant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinzhe.aiassistant.common.result.Result;
import com.xinzhe.aiassistant.common.util.DoubaoUtil;
import com.xinzhe.aiassistant.common.util.UserContext;
import com.xinzhe.aiassistant.dto.ChatMessageDTO;
import com.xinzhe.aiassistant.dto.ChatRequestDTO;
import com.xinzhe.aiassistant.entity.ChatMessage;
import com.xinzhe.aiassistant.entity.ChatSession;
import com.xinzhe.aiassistant.service.ChatMessageService;
import com.xinzhe.aiassistant.service.ChatSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * AI对话核心接口
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private DoubaoUtil doubaoUtil;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * 多轮对话核心接口
     * 接口地址：POST /chat/send
     */
    @PostMapping("/send")
    public Result<String> sendMessage(@RequestBody ChatRequestDTO request) {
        Long userId = UserContext.getCurrentUserId();
        Long sessionId = request.getSessionId();
        String question = request.getQuestion();

        // 1. 基础参数校验
        if (sessionId == null) {
            return Result.fail("会话ID不能为空");
        }
        if (question == null || question.trim().isEmpty()) {
            return Result.fail("提问内容不能为空");
        }

        // 2. 校验会话权限
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return Result.fail("会话不存在或无权限访问");
        }

        // 3. 查询该会话的历史消息，构建发给大模型的消息列表
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getDeleted, 0)
                .orderByAsc(ChatMessage::getCreatedAt);
        List<ChatMessage> historyMessageList = chatMessageService.list(wrapper);

        // 4. 把数据库里的历史消息，转换成大模型要求的DTO格式
        List<ChatMessageDTO> messageList = new ArrayList<>();
        for (ChatMessage msg : historyMessageList) {
            messageList.add(new ChatMessageDTO(msg.getRole(), msg.getContent()));
        }

        // 5. 把当前用户的提问，加入消息列表
        ChatMessageDTO currentQuestion = new ChatMessageDTO("user", question);
        messageList.add(currentQuestion);

        // 6. 调用大模型API，获取AI回答
        String answer = doubaoUtil.chat(messageList);

        // 7. 保存用户提问到数据库
        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(sessionId);
        userMessage.setUserId(userId);
        userMessage.setRole("user");
        userMessage.setContent(question);
        userMessage.setCreatedAt(LocalDateTime.now());
        userMessage.setDeleted(0);
        chatMessageService.save(userMessage);

        // 8. 保存AI回答到数据库
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setSessionId(sessionId);
        assistantMessage.setUserId(userId);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(answer);
        assistantMessage.setCreatedAt(LocalDateTime.now());
        assistantMessage.setDeleted(0);
        chatMessageService.save(assistantMessage);

        // 9. 更新会话的最后更新时间，让会话排在列表最前面
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionService.updateById(session);

        // 10. 返回AI回答给前端
        return Result.success(answer);
    }
}