package com.xinzhe.aiassistant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinzhe.aiassistant.common.result.Result;
import com.xinzhe.aiassistant.common.util.*;
import com.xinzhe.aiassistant.entity.DocumentChunk;
import com.xinzhe.aiassistant.entity.KnowledgeDocument;
import com.xinzhe.aiassistant.service.DocumentChunkService;
import com.xinzhe.aiassistant.service.KnowledgeDocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/document")
@Slf4j
public class DocumentController {

    @Autowired
    private FileUtil fileUtil;

    @Autowired
    private DocumentParserUtil documentParserUtil;

    @Autowired
    private TextSplitterUtil textSplitterUtil;

    @Autowired
    private EmbeddingUtil embeddingUtil;

    @Autowired
    private KnowledgeDocumentService documentService;

    @Autowired
    private DocumentChunkService documentChunkService;

    /**
     * 完整文档上传流程：上传→解析→分块→向量化→存数据库
     */
    @PostMapping("/upload")
    public Result<KnowledgeDocument> uploadDocument(@RequestParam("file") MultipartFile file) {
        log.info("=== Day6 文档上传接口被调用 ===");
        Long userId = UserContext.getCurrentUserId();
        log.info("当前用户ID：{}，文件名：{}", userId, file.getOriginalFilename());

        // 1. 上传文件到本地
        String fileUrl = fileUtil.uploadFile(file);
        String fileName = file.getOriginalFilename();
        String fileType = fileUtil.getFileType(fileName);
        long fileSize = file.getSize();

        // 2. 保存文档基础信息
        KnowledgeDocument document = new KnowledgeDocument();
        document.setUserId(userId);
        document.setFileName(fileName);
        document.setFileType(fileType);
        document.setFileSize(fileSize);
        document.setFileUrl(fileUrl);
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        document.setDeleted(0);
        documentService.save(document);

        // 3. 完整处理流程
        try {
            // 3.1 解析文档内容
            String content = documentParserUtil.parseDocument(fileUrl, fileType);
            document.setContent(content);
            log.info("文档解析完成，内容长度：{}", content.length());

            // 3.2 文本分块
            List<String> chunks = textSplitterUtil.splitText(content);
            log.info("文本分块完成，分块数量：{}", chunks.size());

            // 3.3 逐块向量化，保存到数据库
            for (int i = 0; i < chunks.size(); i++) {
                String chunkText = chunks.get(i);
                log.info("正在处理第{}块，内容：{}", i+1, chunkText);

                // 调用适配后的向量化方法
                String embedding = embeddingUtil.textToEmbedding(chunkText);

                // 保存分块信息
                DocumentChunk chunk = new DocumentChunk();
                chunk.setDocumentId(document.getId());
                chunk.setUserId(userId);
                chunk.setChunkText(chunkText);
                chunk.setChunkNum(i);
                chunk.setEmbedding(embedding);
                chunk.setVectorNorm(0.0);
                chunk.setCreatedAt(LocalDateTime.now());
                chunk.setDeleted(0);
                documentChunkService.save(chunk);
            }

            // 3.4 更新文档信息
            document.setUpdatedAt(LocalDateTime.now());
            documentService.updateById(document);

            log.info("=== Day6 文档上传全流程完成，文档ID：{} ===", document.getId());
            return Result.success(document);

        } catch (Exception e) {
            log.error("文档处理失败", e);
            // 异常处理：更新文档状态，删除本地文件
            document.setUpdatedAt(LocalDateTime.now());
            documentService.updateById(document);
            fileUtil.deleteFile(fileUrl);
            return Result.fail("文档处理失败：" + e.getMessage());
        }
    }

    /**
     * 查询我的文档列表
     */
    @GetMapping("/list")
    public Result<List<KnowledgeDocument>> getMyDocuments() {
        Long userId = UserContext.getCurrentUserId();
        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDocument::getUserId, userId)
                .eq(KnowledgeDocument::getDeleted, 0)
                .orderByDesc(KnowledgeDocument::getCreatedAt);
        return Result.success(documentService.list(wrapper));
    }

    /**
     * 删除文档
     */
    @DeleteMapping("/delete/{documentId}")
    public Result<Void> deleteDocument(@PathVariable Long documentId) {
        Long userId = UserContext.getCurrentUserId();
        KnowledgeDocument document = documentService.getById(documentId);
        if (document == null || !document.getUserId().equals(userId)) {
            return Result.fail("文档不存在或无权限删除");
        }
        // 删除文档和分块
        documentService.removeById(documentId);
        LambdaQueryWrapper<DocumentChunk> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentChunk::getDocumentId, documentId);
        documentChunkService.remove(wrapper);
        // 删除本地文件
        fileUtil.deleteFile(document.getFileUrl());
        return Result.success();
    }
}