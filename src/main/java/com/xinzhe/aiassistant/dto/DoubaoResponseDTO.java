package com.xinzhe.aiassistant.dto;

import lombok.Data;
import java.util.List;

/**
 * 豆包API响应结果DTO
 * 对应API返回的JSON格式
 */
@Data
public class DoubaoResponseDTO {
    /**
     * 响应ID
     */
    private String id;
    /**
     * 响应对象类型
     */
    private String object;
    /**
     * 模型生成的回答内容列表
     */
    private List<Choice> choices;
    /**
     * token用量统计
     */
    private Usage usage;

    /**
     * 回答内容内部类
     */
    @Data
    public static class Choice {
        private Integer index;
        private ChatMessageDTO message;
        private String finish_reason;
    }

    /**
     * token用量内部类
     */
    @Data
    public static class Usage {
        private Integer prompt_tokens;
        private Integer completion_tokens;
        private Integer total_tokens;
    }
}