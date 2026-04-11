package com.xinzhe.aiassistant.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 豆包多模态向量化工具
 */
@Component
@Slf4j
public class EmbeddingUtil {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${doubao.api-key}")
    private String apiKey;

    private static final String EMBEDDING_URL = "https://ark.cn-beijing.volces.com/api/v3/embeddings/multimodal";
    private static final String EMBEDDING_MODEL = "ep-m-20260411181005-jx6jk";

    public String textToEmbedding(String text) {
        try {
            log.info("【向量化】开始处理文本：{}", text);

            // 1. 请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey.trim());

            // 2. 请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", EMBEDDING_MODEL);
            List<Map<String, String>> inputList = new ArrayList<>();
            Map<String, String> textInput = new HashMap<>();
            textInput.put("type", "text");
            textInput.put("text", text.trim());
            inputList.add(textInput);
            requestBody.put("input", inputList);

            String requestJson = objectMapper.writeValueAsString(requestBody);
            log.info("【向量化】请求体：{}", requestJson);

            // 3. 发送请求
            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(EMBEDDING_URL, httpEntity, String.class);
            log.info("【向量化】API响应：{}", response);

            // 4. 解析响应（✅ 核心修复：正确解析向量）
            JsonNode root = objectMapper.readTree(response);
            // 错误判断
            if (root.has("error")) {
                String errorMsg = root.get("error").get("message").asText();
                log.error("【向量化】API报错：{}", errorMsg);
                throw new RuntimeException("向量化API错误：" + errorMsg);
            }

            // ✅ 关键：正确获取 data -> embedding 数组
            JsonNode dataNode = root.get("data");
            if (dataNode == null || !dataNode.has("embedding")) {
                throw new RuntimeException("API返回格式异常：未获取到向量数据");
            }
            JsonNode embeddingNode = dataNode.get("embedding");

            // ✅ 遍历数组，转为Double集合
            List<Double> embeddingList = new ArrayList<>();
            for (JsonNode node : embeddingNode) {
                embeddingList.add(node.asDouble());
            }

            // ✅ 转为逗号分隔字符串（存入数据库用）
            String embeddingStr = embeddingList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            log.info("【向量化】成功！向量长度：{}", embeddingList.size());
            return embeddingStr;

        } catch (Exception e) {
            log.error("【向量化】处理失败", e);
            throw new RuntimeException("文本向量化失败：" + e.getMessage());
        }
    }

    // 字符串转double数组
    public double[] embeddingToDoubleArray(String embeddingStr) {
        if (embeddingStr == null || embeddingStr.isBlank()) return new double[0];
        String[] parts = embeddingStr.split(",");
        double[] vector = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Double.parseDouble(parts[i].trim());
        }
        return vector;
    }

    /**
     * 余弦相似度计算（适配String格式向量 + double[]向量）
     * @param v1 String格式的向量（数据库存储的逗号分隔字符串）
     * @param v2 double[]格式的向量（用户问题向量化后的数组）
     * @return 相似度（0~1，越接近1越相似）
     */
    public double cosineSimilarity(String v1, double[] v2) {
        // 1. 先把String格式的向量转为double[]数组（复用你已有的工具方法）
        double[] arr1 = embeddingToDoubleArray(v1);
        double[] arr2 = v2;

        // 2. 校验向量长度一致，不一致直接返回0
        if (arr1.length != arr2.length || arr1.length == 0) {
            return 0.0;
        }

        double dotProduct = 0.0, norm1 = 0.0, norm2 = 0.0;
        for (int i = 0; i < arr1.length; i++) {
            dotProduct += arr1[i] * arr2[i];
            norm1 += Math.pow(arr1[i], 2);
            norm2 += Math.pow(arr2[i], 2);
        }

        // 避免除以0
        return norm1 == 0 || norm2 == 0 ? 0.0 : dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}