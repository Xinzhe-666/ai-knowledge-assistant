package com.xinzhe.aiassistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("document_chunk")
public class DocumentChunk {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long documentId;
    private Long userId;
    private String chunkText;
    private Integer chunkNum;
    private String embedding;
    private Double vectorNorm;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
}