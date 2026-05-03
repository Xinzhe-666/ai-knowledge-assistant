package com.xinzhe.aiassistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_document")
public class KnowledgeDocument {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String fileName;
    private String fileType;
    private String fileUrl;
    private Long fileSize;
    private String content;
    /**
     * 文档处理状态：
     * PROCESSING：处理中
     * SUCCESS：处理成功
     * FAILED：处理失败
     */
    private String status;

    /**
     * 处理失败时记录错误信息，方便排查问题
     */
    private String errorMessage;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}