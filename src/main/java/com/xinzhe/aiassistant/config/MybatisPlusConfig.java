package com.xinzhe.aiassistant.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 * 配置分页插件、逻辑删除等功能
 */
@Configuration
public class MybatisPlusConfig {

    /**
     * 分页插件配置
     * 必须配置这个Bean，MyBatis-Plus的分页功能才会生效
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加分页插件，指定数据库类型为MySQL
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        // 单页最大条数限制，防止恶意查询全表数据
        paginationInterceptor.setMaxLimit(100L);
        // 溢出总页数后，自动跳回第一页
        paginationInterceptor.setOverflow(true);
        interceptor.addInnerInterceptor(paginationInterceptor);
        return interceptor;
    }
}