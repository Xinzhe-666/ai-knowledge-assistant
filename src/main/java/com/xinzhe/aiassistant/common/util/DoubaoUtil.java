package com.xinzhe.aiassistant.common.util;

import com.xinzhe.aiassistant.dto.ChatMessageDTO;
import com.xinzhe.aiassistant.dto.DoubaoRequestDTO;
import com.xinzhe.aiassistant.dto.DoubaoResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * 豆包大模型API调用工具类
 * 封装大模型调用逻辑，上层业务直接调用
 */
@Component
@Slf4j
public class DoubaoUtil {

    // 注入RestTemplate，用于发送HTTP请求
    @Autowired
    private RestTemplate restTemplate;

    // 从配置文件读取配置
    @Value("${doubao.api-key}")
    private String apiKey;

    @Value("${doubao.api-url}")
    private String apiUrl;

    @Value("${doubao.model}")
    private String model;

    @Value("${doubao.temperature}")
    private Double temperature;

    /**
     * 核心方法：调用大模型，获取AI回答
     * @param messages 消息列表，包含历史消息+当前提问，实现多轮对话
     * @return AI生成的回答内容
     */
    public String chat(List<ChatMessageDTO> messages) {
        // 1. 构建请求头
        HttpHeaders headers = new HttpHeaders();
        // 设置内容类型为JSON
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 设置API Key认证，豆包API要求的认证格式
        headers.set("Authorization", "Bearer " + apiKey);

        // 2. 构建请求体
        DoubaoRequestDTO request = new DoubaoRequestDTO();
        request.setModel(model);
        request.setMessages(messages);
        request.setTemperature(temperature);
        request.setStream(false);

        // 3. 封装HTTP请求
        HttpEntity<DoubaoRequestDTO> httpEntity = new HttpEntity<>(request, headers);

        try {
            // 4. 发送POST请求，调用大模型API
            log.info("开始调用豆包API，消息条数：{}", messages.size());
            DoubaoResponseDTO response = restTemplate.postForObject(apiUrl, httpEntity, DoubaoResponseDTO.class);

            // 5. 解析响应结果，获取AI回答内容
            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                log.error("豆包API响应为空");
                throw new RuntimeException("AI响应异常，请稍后重试");
            }

            String answer = response.getChoices().get(0).getMessage().getContent();
            log.info("豆包API调用成功，token用量：{}", response.getUsage().getTotal_tokens());
            return answer;

        } catch (Exception e) {
            log.error("豆包API调用失败", e);
            throw new RuntimeException("AI服务调用失败，请稍后重试");
        }
    }

    /**
     * 便捷方法：单轮对话，直接传入提问内容，返回回答
     * 用于简单的单轮提问，无需历史上下文
     */
    public String singleChat(String question) {
        ChatMessageDTO message = new ChatMessageDTO("user", question);
        return chat(List.of(message));
    }
}