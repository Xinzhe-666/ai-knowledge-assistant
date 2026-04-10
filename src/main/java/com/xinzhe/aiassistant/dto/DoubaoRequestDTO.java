package com.xinzhe.aiassistant.dto;

import lombok.Data;
import java.util.List;

/**
 * 豆包API请求参数DTO
 * 对应API要求的请求体格式
 */
@Data
public class DoubaoRequestDTO {
    /**
     * 模型名称
     */
    private String model;
    /**
     * 聊天消息列表，包含历史消息+当前提问，实现多轮对话
     */
    private List<ChatMessageDTO> messages;
    /**
     * 温度值，控制回答的发散程度
     */
    private Double temperature;
    /**
     * 是否流式输出，我们先做非流式，后续可以扩展流式输出
     */
    private Boolean stream = false;
}