package com.xinzhe.aiassistant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinzhe.aiassistant.entity.DocumentChunk;
import com.xinzhe.aiassistant.service.DocumentChunkService;
import com.xinzhe.aiassistant.mapper.DocumentChunkMapper;
import org.springframework.stereotype.Service;

/**
* @author LiXinzhe
* @description 针对表【document_chunk(文档分块向量表)】的数据库操作Service实现
* @createDate 2026-04-09 10:09:33
*/
@Service
public class DocumentChunkServiceImpl extends ServiceImpl<DocumentChunkMapper, DocumentChunk>
    implements DocumentChunkService{

}




