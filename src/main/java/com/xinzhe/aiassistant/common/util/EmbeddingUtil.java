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

@Component
@Slf4j
public class EmbeddingUtil {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${doubao.api-key}")
    private String apiKey;

    @Value("${doubao.embedding-url}")
    private String embeddingUrl;

    @Value("${doubao.embedding-model}")
    private String embeddingModel;

    /**
     * 把文本转换成逗号分隔的向量字符串，便于存入 MySQL。
     */
    public String textToEmbedding(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new RuntimeException("向量化文本不能为空");
        }

        try {
            String cleanText = text.trim();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey.trim());

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", embeddingModel);
            requestBody.put("encoding_format", "float");

            /*
             * 多模态 Embedding 接口格式：
             * input: [
             *   {
             *     "type": "text",
             *     "text": "文本内容"
             *   }
             * ]
             */
            List<Map<String, Object>> inputList = new ArrayList<>();

            Map<String, Object> textInput = new HashMap<>();
            textInput.put("type", "text");
            textInput.put("text", cleanText);

            inputList.add(textInput);
            requestBody.put("input", inputList);

            /*
             * 这里手动转 JSON 字符串，避免 RestTemplate 自动序列化导致请求体格式不清晰。
             */
            String requestJson = objectMapper.writeValueAsString(requestBody);
            log.info("Embedding API 请求体：{}", requestJson);

            HttpEntity<String> httpEntity = new HttpEntity<>(requestJson, headers);

            log.info("开始调用 Embedding API，url：{}，model：{}，文本长度：{}",
                    embeddingUrl, embeddingModel, cleanText.length());

            String response = restTemplate.postForObject(embeddingUrl, httpEntity, String.class);

            log.debug("Embedding API 原始响应：{}", response);

            JsonNode root = objectMapper.readTree(response);

            if (root.has("error")) {
                String errorMsg = root.path("error").path("message").asText("未知错误");
                String errorCode = root.path("error").path("code").asText("");
                throw new RuntimeException("向量化 API 错误：" + errorCode + " " + errorMsg);
            }

            JsonNode dataNode = root.path("data");
            if (dataNode.isMissingNode() || dataNode.isNull()) {
                throw new RuntimeException("Embedding API 返回异常：data 不存在，response=" + response);
            }

            JsonNode embeddingNode;

            /*
             * 兼容两种返回格式：
             *
             * 1. 文本 Embedding 接口常见格式：
             * data: [
             *   {
             *     "embedding": [...]
             *   }
             * ]
             *
             * 2. 多模态 Embedding 接口当前返回格式：
             * data: {
             *   "embedding": [...]
             * }
             */
            if (dataNode.isArray()) {
                if (dataNode.isEmpty()) {
                    throw new RuntimeException("Embedding API 返回异常：data 数组为空，response=" + response);
                }
                embeddingNode = dataNode.get(0).path("embedding");
            } else if (dataNode.isObject()) {
                embeddingNode = dataNode.path("embedding");
            } else {
                throw new RuntimeException("Embedding API 返回异常：data 格式不支持，response=" + response);
            }

            /*
             * 兼容二维数组：
             * embedding: [[0.1, 0.2, ...]]
             */
            if (embeddingNode.isArray() && !embeddingNode.isEmpty() && embeddingNode.get(0).isArray()) {
                embeddingNode = embeddingNode.get(0);
            }

            if (!embeddingNode.isArray() || embeddingNode.isEmpty()) {
                throw new RuntimeException("Embedding API 返回异常：未获取到 embedding 数组，response=" + response);
            }

            /*
             * 多模态 Embedding 可能返回二维数组：
             * embedding: [[0.1, 0.2, ...]]
             * 所以这里做一层兼容。
             */
            if (embeddingNode.isArray() && !embeddingNode.isEmpty() && embeddingNode.get(0).isArray()) {
                embeddingNode = embeddingNode.get(0);
            }

            if (!embeddingNode.isArray() || embeddingNode.isEmpty()) {
                throw new RuntimeException("Embedding API 返回异常：未获取到 embedding 数组，response=" + response);
            }

            List<Double> embeddingList = new ArrayList<>();
            for (JsonNode node : embeddingNode) {
                embeddingList.add(node.asDouble());
            }

            log.info("文本向量化成功，向量维度：{}", embeddingList.size());

            return embeddingList.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

        } catch (Exception e) {
            log.error("文本向量化失败", e);
            throw new RuntimeException("文本向量化失败：" + e.getMessage(), e);
        }
    }

    /**
     * 把数据库中的逗号分隔向量字符串转成 double[]。
     */
    public double[] embeddingToDoubleArray(String embeddingStr) {
        if (embeddingStr == null || embeddingStr.isBlank()) {
            return new double[0];
        }

        String[] parts = embeddingStr.split(",");
        double[] vector = new double[parts.length];

        for (int i = 0; i < parts.length; i++) {
            vector[i] = Double.parseDouble(parts[i].trim());
        }

        return vector;
    }

    /**
     * 计算向量模长，用于保存 chunk 时预先计算。
     */
    public double calculateNorm(String embeddingStr) {
        double[] vector = embeddingToDoubleArray(embeddingStr);
        double sum = 0.0;

        for (double v : vector) {
            sum += v * v;
        }

        return Math.sqrt(sum);
    }

    /**
     * 计算余弦相似度。
     * v1：数据库中的向量字符串
     * v2：问题向量 double[]
     */
    public double cosineSimilarity(String v1, double[] v2) {
        double[] arr1 = embeddingToDoubleArray(v1);

        if (arr1.length != v2.length || arr1.length == 0) {
            return 0.0;
        }

        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;

        for (int i = 0; i < arr1.length; i++) {
            dotProduct += arr1[i] * v2[i];
            norm1 += arr1[i] * arr1[i];
            norm2 += v2[i] * v2[i];
        }

        if (norm1 == 0 || norm2 == 0) {
            return 0.0;
        }

        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}