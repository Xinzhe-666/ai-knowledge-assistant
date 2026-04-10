package com.xinzhe.aiassistant.dto;

import lombok.Data;

/**
 * 对话请求DTO
 * 接收前端发送的提问参数
 */
@Data
public class ChatRequestDTO {
    /**
     * 会话ID，必须传，确定是哪个会话的对话
     */
    private Long sessionId;
    /**
     * 用户的提问内容
     */
    private String question;
}