package com.xinzhe.aiassistant.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinzhe.aiassistant.entity.User;
import com.xinzhe.aiassistant.service.UserService;
import com.xinzhe.aiassistant.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
* @author LiXinzhe
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2026-04-09 10:07:11
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

}




