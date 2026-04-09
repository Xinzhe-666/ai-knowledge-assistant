package com.xinzhe.aiassistant.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库的user表，一个User对象对应user表的一行数据
 */
@Data // 自动生成get/set/toString/equals/hashCode方法
@TableName("user") // 告诉MyBatis-Plus这个类对应数据库的user表
public class User {

    @TableId(type = IdType.AUTO) // 标记这个字段是主键，自增类型
    private Long id;

    private String username; // 用户名，对应数据库的username字段

    private String password; // 密码，对应数据库的password字段

    private String nickname; // 昵称，对应数据库的nickname字段

    private String role; // 角色，对应数据库的role字段

    private Integer status; // 账号状态，对应数据库的status字段

    @TableLogic // 标记这个字段是逻辑删除字段
    private Integer deleted; // 逻辑删除，对应数据库的deleted字段

    private LocalDateTime createdAt; // 创建时间，对应数据库的created_at字段

    private LocalDateTime updatedAt; // 更新时间，对应数据库的updated_at字段
}