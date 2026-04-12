```sql
-- ========================================
-- AI知识助手数据库初始化脚本
-- ========================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- 用户表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                        `username` varchar(50) NOT NULL COMMENT '用户名',
                        `password` varchar(100) NOT NULL COMMENT '密码（BCrypt加密）',
                        `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
                        `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除（0-未删除，1-已删除）',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ----------------------------
-- 知识库文档表
-- ----------------------------
DROP TABLE IF EXISTS `knowledge_document`;
CREATE TABLE `knowledge_document` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文档ID',
                                      `user_id` bigint NOT NULL COMMENT '用户ID',
                                      `file_name` varchar(255) NOT NULL COMMENT '文件名',
                                      `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
                                      `file_type` varchar(20) NOT NULL COMMENT '文件类型',
                                      `content` longtext COMMENT '文档纯文本内容',
                                      `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

-- ----------------------------
-- 文档分片表
-- ----------------------------
DROP TABLE IF EXISTS `document_chunk`;
CREATE TABLE `document_chunk` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分片ID',
                                  `document_id` bigint NOT NULL COMMENT '文档ID',
                                  `user_id` bigint NOT NULL COMMENT '用户ID',
                                  `chunk_text` longtext NOT NULL COMMENT '分片文本内容',
                                  `chunk_index` int NOT NULL COMMENT '分片序号',
                                  `embedding` text NOT NULL COMMENT '向量（逗号分隔字符串）',
                                  `vector_norm` double NOT NULL DEFAULT '0' COMMENT '向量模长（预计算优化）',
                                  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_document_id` (`document_id`),
                                  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档分片与向量表';

-- ----------------------------
-- 对话会话表
-- ----------------------------
DROP TABLE IF EXISTS `chat_session`;
CREATE TABLE `chat_session` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
                                `user_id` bigint NOT NULL COMMENT '用户ID',
                                `title` varchar(255) NOT NULL COMMENT '会话标题',
                                `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
                                PRIMARY KEY (`id`),
                                KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对话会话表';

-- ----------------------------
-- 聊天消息表
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
                                `session_id` bigint NOT NULL COMMENT '会话ID',
                                `user_id` bigint NOT NULL COMMENT '用户ID',
                                `role` varchar(20) NOT NULL COMMENT '角色（user/assistant/system）',
                                `content` longtext NOT NULL COMMENT '消息内容',
                                `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `deleted` tinyint NOT NULL DEFAULT '0' COMMENT '逻辑删除',
                                PRIMARY KEY (`id`),
                                KEY `idx_session_id` (`session_id`),
                                KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

SET FOREIGN_KEY_CHECKS = 1;