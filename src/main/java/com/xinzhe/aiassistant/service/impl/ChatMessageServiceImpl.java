package com.xinzhe.aiassistant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinzhe.aiassistant.entity.ChatMessage;
import com.xinzhe.aiassistant.service.ChatMessageService;
import com.xinzhe.aiassistant.mapper.ChatMessageMapper;
import org.springframework.stereotype.Service;

/**
* @author LiXinzhe
* @description 针对表【chat_message(聊天消息表)】的数据库操作Service实现
* @createDate 2026-04-09 10:09:25
*/
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
    implements ChatMessageService{

}




