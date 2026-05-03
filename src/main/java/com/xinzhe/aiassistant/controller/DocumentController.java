package com.xinzhe.aiassistant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xinzhe.aiassistant.common.result.Result;
import com.xinzhe.aiassistant.common.util.DocumentParserUtil;
import com.xinzhe.aiassistant.common.util.EmbeddingUtil;
import com.xinzhe.aiassistant.common.util.FileUtil;
import com.xinzhe.aiassistant.common.util.TextSplitterUtil;
import com.xinzhe.aiassistant.common.util.UserContext;
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
     * 文档上传完整流程：
     * 上传文件 -> 保存文档记录 -> 解析文本 -> 分块 -> 向量化 -> 保存 chunk
     */
    @PostMapping("/upload")
    public Result<KnowledgeDocument> uploadDocument(@RequestParam("file") MultipartFile file) {
        Long userId = UserContext.getCurrentUserId();

        if (file == null || file.isEmpty()) {
            return Result.fail("上传文件不能为空");
        }

        String fileUrl = null;
        KnowledgeDocument document = new KnowledgeDocument();

        try {
            log.info("文档上传开始，用户ID：{}，文件名：{}", userId, file.getOriginalFilename());

            fileUrl = fileUtil.uploadFile(file);
            String fileName = file.getOriginalFilename();
            String fileType = fileUtil.getFileType(fileName);
            long fileSize = file.getSize();

            document.setUserId(userId);
            document.setFileName(fileName);
            document.setFileType(fileType);
            document.setFileSize(fileSize);
            document.setFileUrl(fileUrl);
            document.setStatus("PROCESSING");
            document.setErrorMessage(null);
            document.setCreatedAt(LocalDateTime.now());
            document.setUpdatedAt(LocalDateTime.now());
            document.setDeleted(0);

            documentService.save(document);

            String content = documentParserUtil.parseDocument(fileUrl, fileType);
            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("文档解析结果为空");
            }

            document.setContent(content);
            log.info("文档解析完成，文档ID：{}，内容长度：{}", document.getId(), content.length());

            List<String> chunks = textSplitterUtil.splitText(content);
            if (chunks.isEmpty()) {
                throw new RuntimeException("文本分块结果为空");
            }

            log.info("文本分块完成，文档ID：{}，分块数量：{}", document.getId(), chunks.size());

            for (int i = 0; i < chunks.size(); i++) {
                String chunkText = chunks.get(i);
                String embedding = embeddingUtil.textToEmbedding(chunkText);

                DocumentChunk chunk = new DocumentChunk();
                chunk.setDocumentId(document.getId());
                chunk.setUserId(userId);
                chunk.setChunkText(chunkText);
                chunk.setChunkNum(i);
                chunk.setEmbedding(embedding);
                chunk.setVectorNorm(embeddingUtil.calculateNorm(embedding));
                chunk.setCreatedAt(LocalDateTime.now());
                chunk.setDeleted(0);

                documentChunkService.save(chunk);
            }

            document.setStatus("SUCCESS");
            document.setErrorMessage(null);
            document.setUpdatedAt(LocalDateTime.now());
            documentService.updateById(document);

            log.info("文档上传处理成功，文档ID：{}", document.getId());
            return Result.success(document);

        } catch (Exception e) {
            log.error("文档处理失败", e);

            if (document.getId() != null) {
                document.setStatus("FAILED");
                document.setErrorMessage(e.getMessage());
                document.setUpdatedAt(LocalDateTime.now());
                documentService.updateById(document);
            }

            if (fileUrl != null) {
                fileUtil.deleteFile(fileUrl);
            }

            return Result.fail("文档处理失败：" + e.getMessage());
        }
    }

    /**
     * 查询当前用户的文档列表。
     */
    @GetMapping("/list")
    public Result<List<KnowledgeDocument>> getMyDocuments() {
        Long userId = UserContext.getCurrentUserId();

        LambdaQueryWrapper<KnowledgeDocument> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDocument::getUserId, userId).eq(KnowledgeDocument::getDeleted, 0).orderByDesc(KnowledgeDocument::getCreatedAt);

        return Result.success(documentService.list(wrapper));
    }

    /**
     * 删除文档，同时删除对应 chunk 和本地文件。
     */
    @DeleteMapping("/delete/{documentId}")
    public Result<Void> deleteDocument(@PathVariable Long documentId) {
        Long userId = UserContext.getCurrentUserId();

        KnowledgeDocument document = documentService.getById(documentId);
        if (document == null || !document.getUserId().equals(userId)) {
            return Result.fail("文档不存在或无权限删除");
        }

        documentService.removeById(documentId);

        LambdaQueryWrapper<DocumentChunk> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DocumentChunk::getDocumentId, documentId).eq(DocumentChunk::getUserId, userId);

        documentChunkService.remove(wrapper);

        if (document.getFileUrl() != null) {
            fileUtil.deleteFile(document.getFileUrl());
        }

        return Result.success();
    }
}
