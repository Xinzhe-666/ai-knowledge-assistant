package com.xinzhe.aiassistant.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinzhe.aiassistant.common.result.Result;
import com.xinzhe.aiassistant.common.util.DoubaoUtil;
import com.xinzhe.aiassistant.common.util.UserContext;
import com.xinzhe.aiassistant.dto.ChatMessageDTO;
import com.xinzhe.aiassistant.dto.ChatRequestDTO;
import com.xinzhe.aiassistant.entity.ChatMessage;
import com.xinzhe.aiassistant.entity.ChatSession;
import com.xinzhe.aiassistant.service.ChatMessageService;
import com.xinzhe.aiassistant.service.ChatSessionService;
import com.xinzhe.aiassistant.service.DocumentChunkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.xinzhe.aiassistant.common.util.EmbeddingUtil;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Comparator;
import com.xinzhe.aiassistant.entity.DocumentChunk;

/**
 * AI对话核心接口
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private DoubaoUtil doubaoUtil;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private EmbeddingUtil embeddingUtil;

    @Autowired
    private DocumentChunkService documentChunkService;

    /**
     * 多轮对话核心接口
     * 接口地址：POST /chat/send
     */
    @PostMapping("/send")
    public Result<String> sendMessage(@RequestBody ChatRequestDTO request) {
        Long userId = UserContext.getCurrentUserId();
        Long sessionId = request.getSessionId();
        String question = request.getQuestion();

        // 1. 基础参数校验
        if (sessionId == null) {
            return Result.fail("会话ID不能为空");
        }
        if (question == null || question.trim().isEmpty()) {
            return Result.fail("提问内容不能为空");
        }

        // 2. 校验会话权限
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return Result.fail("会话不存在或无权限访问");
        }

        // 3. 查询该会话的历史消息，构建发给大模型的消息列表
        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getSessionId, sessionId)
                .eq(ChatMessage::getDeleted, 0)
                .orderByAsc(ChatMessage::getCreatedAt);
        List<ChatMessage> historyMessageList = chatMessageService.list(wrapper);

        // 4. 把数据库里的历史消息，转换成大模型要求的DTO格式
        List<ChatMessageDTO> messageList = new ArrayList<>();
        for (ChatMessage msg : historyMessageList) {
            messageList.add(new ChatMessageDTO(msg.getRole(), msg.getContent()));
        }

        // 5. 把当前用户的提问，加入消息列表
        ChatMessageDTO currentQuestion = new ChatMessageDTO("user", question);
        messageList.add(currentQuestion);

        // 6. 调用大模型API，获取AI回答
        String answer = doubaoUtil.chat(messageList);

        // 7. 保存用户提问到数据库
        ChatMessage userMessage = new ChatMessage();
        userMessage.setSessionId(sessionId);
        userMessage.setUserId(userId);
        userMessage.setRole("user");
        userMessage.setContent(question);
        userMessage.setCreatedAt(LocalDateTime.now());
        userMessage.setDeleted(0);
        chatMessageService.save(userMessage);

        // 8. 保存AI回答到数据库
        ChatMessage assistantMessage = new ChatMessage();
        assistantMessage.setSessionId(sessionId);
        assistantMessage.setUserId(userId);
        assistantMessage.setRole("assistant");
        assistantMessage.setContent(answer);
        assistantMessage.setCreatedAt(LocalDateTime.now());
        assistantMessage.setDeleted(0);
        chatMessageService.save(assistantMessage);

        // 9. 更新会话的最后更新时间，让会话排在列表最前面
        session.setUpdatedAt(LocalDateTime.now());
        chatSessionService.updateById(session);

        // 10. 返回AI回答给前端
        return Result.success(answer);
    }
    /**
     * 处理 /chat/rag POST 请求
     */
    @PostMapping("/rag")
    @SentinelResource(value = "ragChat", blockHandler = "ragChatBlockHandler")
    public Result<String> ragChat(@RequestBody ChatRequestDTO request) {
        /**
         * RAG对话核心接口
         * 接口地址：POST /chat/rag
         * 完整流程：用户提问 → 向量化 → 检索相似文档 → 拼上下文 → 大模型回答 → 存对话 → 返回结果
         */
            // ====================== 1. 基础参数校验 ======================
            Long userId = UserContext.getCurrentUserId(); // 从上下文拿当前登录用户ID（你已有的工具类）
            Long sessionId = request.getSessionId();
            String question = request.getQuestion();

            // 非空校验
            if (sessionId == null) {
                return Result.fail("会话ID不能为空");
            }
            if (question == null || question.trim().isEmpty()) {
                return Result.fail("提问内容不能为空");
            }

            // ====================== 2. 校验会话权限（复用你sendMessage的逻辑） ======================
            ChatSession session = chatSessionService.getById(sessionId);
            if (session == null || !session.getUserId().equals(userId)) {
                return Result.fail("会话不存在或无权限访问");
            }

            // ====================== 3. 用户问题向量化（复用你已有的EmbeddingUtil） ======================
            // 把用户问题转换成向量，用于后续相似度计算
            // 🔴 注意：根据你的EmbeddingUtil方法签名二选一：
            // 情况1：textToEmbedding返回double[]（推荐）
            String questionEmbedding = embeddingUtil.textToEmbedding(question);
            double[] questionVector = embeddingUtil.embeddingToDoubleArray(questionEmbedding);
            // 情况2：textToEmbedding返回String（数据库存字符串向量），用下面两行替换上面一行
            // String questionEmbeddingStr = embeddingUtil.textToEmbedding(question);
            // double[] questionVector = embeddingUtil.embeddingToDoubleArray(questionEmbeddingStr);

            // ====================== 4. 检索当前用户的文档切片（核心：用户隔离！） ======================
            // 用MyBatis-Plus lambda查询，只查当前用户的切片，绝对不能查全量！
            List<DocumentChunk> userChunks = documentChunkService.lambdaQuery()
                    .eq(DocumentChunk::getUserId, userId) // 按用户ID过滤，实现数据隔离
                    .eq(DocumentChunk::getDeleted, 0) // 过滤未删除的切片
                    .list();

            // 知识库为空直接返回
            if (userChunks.isEmpty()) {
                return Result.fail("您的知识库为空，请先上传文档后再提问");
            }

            // ====================== 5. 计算余弦相似度，取Top5最相关切片 ======================
            Map<DocumentChunk, Double> similarityMap = new HashMap<>();
            for (DocumentChunk chunk : userChunks) {
                // 把数据库里的String向量转成double数组
                double[] chunkVector = embeddingUtil.embeddingToDoubleArray(chunk.getEmbedding());
                // 计算相似度（你已有的工具类方法）
                double similarity = embeddingUtil.cosineSimilarity(chunk.getEmbedding(), questionVector);
                similarityMap.put(chunk, similarity);
            }


        final int TOP_K = 5;
        final double SIMILARITY_THRESHOLD = 0.30;

        List<DocumentChunk> top5Chunks = similarityMap.entrySet().stream()
                .filter(entry -> entry.getValue() >= SIMILARITY_THRESHOLD)
                .sorted(Map.Entry.<DocumentChunk, Double>comparingByValue(Comparator.reverseOrder()))
                .limit(TOP_K)
                .map(Map.Entry::getKey)
                .toList();

        if (top5Chunks.isEmpty()) {
            return Result.success("抱歉，知识库中没有找到相关信息");
        }

            // ====================== 6. 拼接RAG上下文（给大模型的参考内容） ======================
            StringBuilder context = new StringBuilder("### 参考知识库内容：\n");
            for (int i = 0; i < top5Chunks.size(); i++) {
                context.append(i + 1).append(". ").append(top5Chunks.get(i).getChunkText()).append("\n\n");
            }

            // ====================== 7. 构建大模型消息列表（复用你现有的ChatMessageDTO） ======================
            List<ChatMessageDTO> messageList = new ArrayList<>();

            // 🔴 系统提示词（核心！严格约束大模型，禁止瞎编）
            messageList.add(new ChatMessageDTO("system",
                    "你是豆包AI知识助手，严格基于用户提供的【参考知识库内容】回答问题。\n" +
                            "规则：\n" +
                            "1. 只能使用参考内容中的信息，绝对不能编造、脑补任何内容\n" +
                            "2. 如果参考内容中没有相关信息，直接回答「抱歉，知识库中没有相关内容，无法回答您的问题」\n" +
                            "3. 回答要简洁、准确，符合用户问题的需求\n" +
                            "4. 不要提及「参考内容」「知识库」等字样，直接给出答案\n"
            ));

            // 拼接上下文+用户问题，发给大模型
            String userPrompt = context + "\n### 用户问题：\n" + question;
            messageList.add(new ChatMessageDTO("user", userPrompt));

            // ====================== 8. 调用大模型API（复用你现有的DoubaoUtil） ======================
            String answer = doubaoUtil.chat(messageList);

            // ====================== 9. 保存对话历史（复用你sendMessage的逻辑，统一会话） ======================
            // 保存用户提问
            ChatMessage userMsg = new ChatMessage();
            userMsg.setSessionId(sessionId);
            userMsg.setUserId(userId);
            userMsg.setRole("user");
            userMsg.setContent(question);
            userMsg.setCreatedAt(LocalDateTime.now());
            userMsg.setDeleted(0);
            chatMessageService.save(userMsg);

            // 保存AI回答
            ChatMessage assistantMsg = new ChatMessage();
            assistantMsg.setSessionId(sessionId);
            assistantMsg.setUserId(userId);
            assistantMsg.setRole("assistant");
            assistantMsg.setContent(answer);
            assistantMsg.setCreatedAt(LocalDateTime.now());
            assistantMsg.setDeleted(0);
            chatMessageService.save(assistantMsg);

            // ====================== 10. 更新会话最后更新时间（复用你sendMessage的逻辑） ======================
            session.setUpdatedAt(LocalDateTime.now());
            chatSessionService.updateById(session);

            // ====================== 11. 统一返回结果 ======================
            return Result.success(answer);
        }
        // 后续逻辑写在这里
        // 限流降级方法
        public Result<String> ragChatBlockHandler(ChatRequestDTO request, BlockException e) {
            return Result.fail("请求过于频繁，请稍后再试");
        }
}