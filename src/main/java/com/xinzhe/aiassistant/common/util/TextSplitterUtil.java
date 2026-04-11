package com.xinzhe.aiassistant.common.util;

import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * 文本分块工具类（修正版，彻底解决死循环和内存溢出）
 */
@Component
public class TextSplitterUtil {

    /**
     * 每块文本的字符数（改小一点，避免单块太大）
     */
    private static final int CHUNK_SIZE = 300;

    /**
     * 拆分文本（安全版）
     * @param text 原始文本
     * @return 拆分后的文本块列表
     */
    public List<String> splitText(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.trim().isEmpty()) {
            return chunks;
        }

        // 清洗文本：去除多余的空白字符
        text = text.replaceAll("\\s+", " ").trim();

        int length = text.length();
        // 如果文本很短，直接返回
        if (length <= CHUNK_SIZE) {
            chunks.add(text);
            return chunks;
        }

        // 安全的分块逻辑，绝对不会死循环
        int offset = 0;
        while (offset < length) {
            // 计算当前块的结束位置
            int end = Math.min(offset + CHUNK_SIZE, length);
            // 截取文本
            String chunk = text.substring(offset, end);
            chunks.add(chunk);
            // 移动偏移量
            offset = end;
        }

        return chunks;
    }
}