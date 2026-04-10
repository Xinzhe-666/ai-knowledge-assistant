package com.xinzhe.aiassistant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单条聊天消息DTO
 * 对应大模型API里的messages数组里的单条消息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO {
    /**
     * 消息角色：user=用户提问，assistant=AI回答
     */
    private String role;
    /**
     * 消息内容
     */
    private String content;
}