package com.xinzhe.aiassistant.common.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class TextSplitterUtil {

    /**
     * 每个 chunk 的最大字符数。
     * 学生项目用字符数切分比 token 切分更简单，便于解释。
     */
    private static final int CHUNK_SIZE = 500;

    /**
     * 相邻 chunk 的重叠字符数，避免上下文被切断。
     */
    private static final int OVERLAP = 80;

    public List<String> splitText(String text) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.trim().isEmpty()) {
            return chunks;
        }

        text = text.replaceAll("\\s+", " ").trim();

        if (text.length() <= CHUNK_SIZE) {
            chunks.add(text);
            return chunks;
        }

        int start = 0;

        while (start < text.length()) {
            int end = Math.min(start + CHUNK_SIZE, text.length());
            String chunk = text.substring(start, end).trim();

            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            if (end == text.length()) {
                break;
            }

            start = end - OVERLAP;
        }

        return chunks;
    }
}