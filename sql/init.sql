SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `chat_message`;
DROP TABLE IF EXISTS `chat_session`;
DROP TABLE IF EXISTS `document_chunk`;
DROP TABLE IF EXISTS `knowledge_document`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
                        `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
                        `username` varchar(50) NOT NULL COMMENT '用户名',
                        `password` varchar(100) NOT NULL COMMENT '密码（BCrypt加密）',
                        `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
                        `role` varchar(20) NOT NULL DEFAULT 'user' COMMENT '角色',
                        `status` tinyint NOT NULL DEFAULT 1 COMMENT '状态：1正常，0禁用',
                        `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                        `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                        `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除，1已删除',
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `knowledge_document` (
                                      `id` bigint NOT NULL AUTO_INCREMENT COMMENT '文档ID',
                                      `user_id` bigint NOT NULL COMMENT '用户ID',
                                      `file_name` varchar(255) NOT NULL COMMENT '文件名',
                                      `file_type` varchar(20) NOT NULL COMMENT '文件类型',
                                      `file_url` varchar(500) DEFAULT NULL COMMENT '文件路径',
                                      `file_size` bigint NOT NULL COMMENT '文件大小',
                                      `content` longtext COMMENT '文档纯文本内容',
                                      `status` varchar(20) NOT NULL DEFAULT 'PROCESSING' COMMENT '处理状态：PROCESSING/SUCCESS/FAILED',
                                      `error_message` text COMMENT '错误信息',
                                      `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='知识库文档表';

CREATE TABLE `document_chunk` (
                                  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '分片ID',
                                  `document_id` bigint NOT NULL COMMENT '文档ID',
                                  `user_id` bigint NOT NULL COMMENT '用户ID',
                                  `chunk_text` longtext NOT NULL COMMENT '分片文本',
                                  `chunk_num` int NOT NULL COMMENT '分片序号',
                                  `embedding` longtext NOT NULL COMMENT '向量字符串',
                                  `vector_norm` double NOT NULL DEFAULT 0 COMMENT '向量模长',
                                  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_document_id` (`document_id`),
                                  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文档分片表';

CREATE TABLE `chat_session` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '会话ID',
                                `user_id` bigint NOT NULL COMMENT '用户ID',
                                `session_name` varchar(255) NOT NULL COMMENT '会话名称',
                                `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                PRIMARY KEY (`id`),
                                KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天会话表';

CREATE TABLE `chat_message` (
                                `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
                                `session_id` bigint NOT NULL COMMENT '会话ID',
                                `user_id` bigint NOT NULL COMMENT '用户ID',
                                `role` varchar(20) NOT NULL COMMENT '角色：user/assistant/system',
                                `content` longtext NOT NULL COMMENT '消息内容',
                                `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                `deleted` tinyint NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                PRIMARY KEY (`id`),
                                KEY `idx_session_id` (`session_id`),
                                KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';

SET FOREIGN_KEY_CHECKS = 1;