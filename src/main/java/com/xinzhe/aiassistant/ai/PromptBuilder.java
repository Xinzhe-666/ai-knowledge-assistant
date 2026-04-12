package com.xinzhe.aiassistant.ai;


import com.xinzhe.aiassistant.dto.ChatMessageDTO;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class PromptBuilder {
    private static final String RAG_PROMPT = "你是专业的知识库助手，必须严格基于下方参考文档回答问题。\n" +
            "规则：\n1. 只能使用参考文档中的信息，绝对不能编造\n" +
            "2. 无相关内容时，直接回答「抱歉，知识库中没有找到相关信息」\n" +
            "3. 回答简洁准确，不要输出无关内容";

    public List<ChatMessageDTO> buildRagPrompt(String question, List<String> chunks) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            context.append("参考文档").append(i+1).append(":\n").append(chunks.get(i)).append("\n\n");
        }
        String fullPrompt = RAG_PROMPT + "\n参考文档：\n" + context + "\n用户问题：" + question;
        return List.of(new ChatMessageDTO("user", fullPrompt));
    }
}
