package com.xinzhe.aiassistant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 项目启动类，Spring Boot的入口，就像汽车的发动机
 */
@SpringBootApplication
@MapperScan("com.xinzhe.aiassistant.mapper")  // 告诉MyBatis-Plus去哪里找Mapper接口
public class AiKnowledgeAssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiKnowledgeAssistantApplication.class, args);
    }

}
