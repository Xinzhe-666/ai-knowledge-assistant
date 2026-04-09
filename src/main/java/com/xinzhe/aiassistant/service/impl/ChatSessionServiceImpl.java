package com.xinzhe.aiassistant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinzhe.aiassistant.entity.ChatSession;
import com.xinzhe.aiassistant.service.ChatSessionService;
import com.xinzhe.aiassistant.mapper.ChatSessionMapper;
import org.springframework.stereotype.Service;

/**
* @author LiXinzhe
* @description 针对表【chat_session(对话会话表)】的数据库操作Service实现
* @createDate 2026-04-09 10:09:29
*/
@Service
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession>
    implements ChatSessionService{

}




