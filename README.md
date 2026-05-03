# AI Knowledge Assistant 后端系统

基于 **Spring Boot 3 + 豆包大模型 API** 的轻量级 RAG 私有知识库后端系统。

本项目支持用户注册登录、JWT 鉴权、文档上传、文本解析、文本分块、Embedding 向量化、相似度检索、知识库问答、多轮会话记录、接口限流和 Docker Compose 部署。

---

## 一、项目定位

本项目是一个面向学习、实习面试和个人知识库场景的 Java 后端 + AI 应用项目。

项目重点不在于堆砌复杂组件，而是完整打通：

```text
用户登录
  -> 上传文档
  -> 文档解析
  -> 文本分块
  -> Embedding 向量化
  -> 向量数据持久化
  -> 用户提问
  -> 相似度检索
  -> 拼接上下文
  -> 调用大模型生成回答
```

当前版本采用 **MySQL 存储文档切片和向量字符串**，在查询时通过 Java 计算余弦相似度，适合小规模个人知识库和学生项目演示。

如果后续数据规模扩大，可以升级为：

- pgvector
- Milvus
- Elasticsearch dense_vector
- 其他向量数据库或向量索引方案

---

## 二、核心功能

### 1. 用户认证

- 用户注册
- 用户登录
- BCrypt 密码加密
- JWT 无状态认证
- 拦截器解析 Token
- ThreadLocal 保存当前请求用户 ID
- 请求结束后清理 ThreadLocal，避免内存泄漏

### 2. 多轮会话

- 创建会话
- 查询会话列表
- 保存用户消息
- 保存 AI 回复
- 查询历史消息
- 普通对话接口支持携带历史上下文调用大模型

### 3. 文档管理

- 支持文档上传
- 支持 TXT、Markdown、PDF、DOCX 等常见文本类文档解析
- 保存文档基础信息
- 保存文档解析后的纯文本内容
- 支持查询当前用户上传的文档
- 支持删除文档及其对应文本切片

### 4. 轻量级 RAG 知识库问答

RAG 流程包括两个阶段。

#### 阶段一：文档入库

```text
上传文档
  -> 保存文件
  -> 解析文本
  -> 文本分块
  -> 调用 Embedding API 生成向量
  -> 保存 chunk 文本和 embedding
```

#### 阶段二：知识库问答

```text
用户提问
  -> 问题向量化
  -> 查询当前用户的文档切片
  -> 计算余弦相似度
  -> 选取 TopK 相关内容
  -> 拼接 Prompt 上下文
  -> 调用大模型生成回答
  -> 保存问答记录
```

### 5. 接口限流

- 使用 Sentinel 对高成本接口进行限流保护
- RAG 问答接口涉及外部大模型调用，延迟和成本较高
- 限流用于保护系统稳定性和 API 调用额度

### 6. Docker Compose 部署

- 支持后端服务、MySQL、Redis 一键编排
- 降低本地部署和演示成本
- 方便面试或答辩时快速展示项目

---

## 三、技术栈

| 模块 | 技术 |
|---|---|
| 后端框架 | Spring Boot 3 |
| ORM | MyBatis-Plus |
| 数据库 | MySQL 8 |
| 缓存/扩展 | Redis |
| 用户认证 | JWT |
| 密码加密 | BCrypt |
| 大模型 | 豆包大模型 API |
| 向量化 | 豆包 Embedding API |
| 接口限流 | Sentinel |
| 文档解析 | Apache POI、PDFBox |
| 接口文档 | SpringDoc OpenAPI |
| 部署 | Docker Compose |
| 构建工具 | Maven |

---

## 四、项目亮点

### 1. 完整的 RAG 主链路

项目完整实现了从文档上传、文本解析、文本分块、向量化、相似度检索到大模型回答的完整流程。

这不是单纯调用大模型接口，而是在大模型回答前加入了用户自己的文档内容，降低模型胡编乱造的概率。

---

### 2. 用户级数据隔离

系统不会相信前端传入的 userId。

登录成功后，后端通过 JWT 拦截器解析用户身份，并将当前用户 ID 存入 ThreadLocal。

后续上传文档、创建会话、查询文档、RAG 检索时，都从后端上下文中获取当前用户 ID。

这样可以避免用户访问到其他人的文档和聊天记录。

---

### 3. 轻量级向量检索实现

当前版本没有直接引入 Milvus 等向量数据库，而是采用：

```text
MySQL 保存 chunk 文本和 embedding 字符串
Java 内存中计算余弦相似度
TopK 相关片段拼接到 Prompt
```

这个方案适合小规模个人知识库，优点是：

- 实现简单
- 方便调试
- 便于理解 RAG 原理
- 适合学生项目和实习面试展示

后续如果数据量增大，可以升级为真正的向量数据库或向量索引方案。

---

### 4. 文本分块带 overlap

文档解析后，系统不会把整篇文档一次性传给大模型，而是先切分成多个文本块。

当前采用固定长度 + overlap 的分块方式：

```text
chunk1: 0 - 500
chunk2: 420 - 920
chunk3: 840 - 1340
```

相邻文本块之间保留一部分重叠内容，可以减少上下文被硬切断的问题。

---

### 5. API Key 与敏感配置隔离

项目不会提交真实 API Key、数据库密码、JWT 密钥等敏感信息。

运行时需要通过环境变量注入：

- DOUBAO_API_KEY
- JWT_SECRET
- SPRING_DATASOURCE_PASSWORD

仓库中只保留 `.env.example` 和配置模板文件。

---

## 五、项目结构

```text
ai-knowledge-assistant
├── src
│   └── main
│       ├── java
│       │   └── com.xinzhe.aiassistant
│       │       ├── common
│       │       │   ├── result
│       │       │   └── util
│       │       ├── config
│       │       ├── controller
│       │       ├── entity
│       │       ├── mapper
│       │       └── service
│       └── resources
│           ├── application.yml
│           └── application-dev.yml.example
├── sql
│   └── init.sql
├── docker-compose.yml
├── Dockerfile
├── .env.example
├── .gitignore
├── pom.xml
└── README.md
```

---

## 六、核心业务流程

### 1. 登录认证流程

```text
用户登录
  -> 校验用户名和密码
  -> BCrypt 校验密码
  -> 生成 JWT
  -> 前端保存 Token
  -> 后续请求携带 Authorization: Bearer Token
  -> 拦截器解析 Token
  -> 保存 userId 到 ThreadLocal
  -> Controller/Service 获取当前用户 ID
  -> 请求结束后清理 ThreadLocal
```

---

### 2. 文档上传流程

```text
用户上传文件
  -> 保存文件到本地目录
  -> 保存文档基础信息
  -> 解析文件内容
  -> 清洗文本
  -> 文本分块
  -> 每个 chunk 调用 Embedding API
  -> 保存 chunkText、embedding、documentId、userId
  -> 文档状态更新为 SUCCESS
```

如果处理失败：

```text
文档状态更新为 FAILED
记录 errorMessage
删除已上传的本地文件
返回错误信息
```

---

### 3. RAG 问答流程

```text
用户提问
  -> 问题文本向量化
  -> 查询当前用户全部 chunk
  -> 计算问题向量与 chunk 向量的余弦相似度
  -> 根据相似度排序
  -> 取 TopK 相关 chunk
  -> 拼接知识库上下文
  -> 构造 Prompt
  -> 调用豆包大模型
  -> 保存用户问题和 AI 回答
  -> 返回回答结果
```

---

## 七、本地运行

### 1. 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 6+
- Docker，可选
- 豆包大模型 API Key

---

### 2. 创建数据库

```sql
CREATE DATABASE ai_knowledge_assistant
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

---

### 3. 初始化表结构

在项目根目录执行：

```bash
mysql -u root -p ai_knowledge_assistant < sql/init.sql
```

---

### 4. 配置环境变量

#### Windows PowerShell

```powershell
$env:DOUBAO_API_KEY="你的豆包API Key"
$env:JWT_SECRET="xinzhe-ai-assistant-change-this-secret-at-least-32-chars"
$env:SPRING_DATASOURCE_PASSWORD="你的MySQL密码"
```

#### Mac / Linux

```bash
export DOUBAO_API_KEY="你的豆包API Key"
export JWT_SECRET="xinzhe-ai-assistant-change-this-secret-at-least-32-chars"
export SPRING_DATASOURCE_PASSWORD="你的MySQL密码"
```

---

### 5. 编译项目

```bash
mvn clean package
```

看到以下结果说明编译成功：

```text
BUILD SUCCESS
```

---

### 6. 启动项目

```bash
mvn spring-boot:run
```

默认启动地址：

```text
http://localhost:8080
```

---

## 八、Docker Compose 启动

### 1. 复制环境变量模板

```bash
cp .env.example .env
```

Windows 用户可以手动复制 `.env.example`，然后重命名为 `.env`。

---

### 2. 修改 `.env`

```env
SPRING_DATASOURCE_PASSWORD=你的MySQL密码
DOUBAO_API_KEY=你的豆包API Key
JWT_SECRET=xinzhe-ai-assistant-change-this-secret-at-least-32-chars
FILE_UPLOAD_PATH=./uploads
```

---

### 3. 启动服务

```bash
docker compose up -d --build
```

---

### 4. 查看服务状态

```bash
docker compose ps
```

---

### 5. 查看日志

```bash
docker compose logs -f ai-knowledge-assistant
```

---

## 九、接口测试流程

建议使用 Apifox、Postman 或 IDEA HTTP Client 测试。

---

### 1. 用户注册

```http
POST http://localhost:8080/auth/register
Content-Type: application/json

{
  "username": "test001",
  "password": "123456",
  "nickname": "测试用户"
}
```

---

### 2. 用户登录

```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "test001",
  "password": "123456"
}
```

登录成功后复制返回的 Token。

---

### 3. 创建会话

```http
POST http://localhost:8080/session/create
Authorization: Bearer 你的token
Content-Type: application/json

{
  "sessionName": "RAG测试会话"
}
```

---

### 4. 上传文档

```http
POST http://localhost:8080/document/upload
Authorization: Bearer 你的token
Content-Type: multipart/form-data

file: 选择一个 txt / md / pdf / docx 文件
```

建议先用 `.txt` 文件测试，txt 跑通后再测试 PDF 和 DOCX。

---

### 5. RAG 提问

```http
POST http://localhost:8080/chat/rag
Authorization: Bearer 你的token
Content-Type: application/json

{
  "sessionId": 1,
  "question": "这篇文档主要讲了什么？"
}
```

---

### 6. 查询历史消息

```http
GET http://localhost:8080/message/list/1
Authorization: Bearer 你的token
```

---

## 十、环境变量说明

| 变量名 | 说明 | 示例 |
|---|---|---|
| DOUBAO_API_KEY | 豆包大模型 API Key | 不要提交到 GitHub |
| JWT_SECRET | JWT 签名密钥 | 至少 32 位 |
| SPRING_DATASOURCE_PASSWORD | MySQL 密码 | 本地数据库密码 |
| FILE_UPLOAD_PATH | 文件上传目录 | ./uploads |
| REDIS_HOST | Redis 地址 | localhost |
| REDIS_PORT | Redis 端口 | 6379 |

---

## 十一、安全说明

本项目遵循以下安全原则：

1. 不提交真实 API Key。
2. 不提交真实数据库密码。
3. 不提交真实 JWT 密钥。
4. 本地上传文件目录不提交 GitHub。
5. 日志文件不提交 GitHub。
6. 使用 BCrypt 存储密码哈希。
7. 使用 JWT 进行无状态认证。
8. 后端通过 ThreadLocal 获取当前用户身份，不信任前端传入的 userId。

---

## 十二、当前版本的设计取舍

### 1. 为什么没有使用 Milvus？

当前项目定位是实习级轻量 RAG 项目，优先保证主链路完整和可解释。

MySQL 存储 embedding 字符串的方式适合小规模个人知识库，便于调试和理解。

当文档切片数量明显增大时，当前方案会出现全量扫描和计算相似度的性能瓶颈。后续可以升级为 Milvus、pgvector 或 Elasticsearch dense_vector。

---

### 2. 为什么使用固定长度 + overlap 分块？

复杂语义分块需要更高的实现成本和更多调参。

当前项目采用固定长度 + overlap 的方式，能在控制复杂度的同时减少上下文被切断的问题，适合学生项目和面试讲解。

---

### 3. Redis 当前主要作用是什么？

当前项目中 Redis 主要作为基础设施和后续扩展点。

可扩展方向包括：

- JWT 黑名单
- 热点文档缓存
- 高频问题缓存
- Embedding 结果缓存
- 用户会话缓存

当前版本不会把 Redis 包装成完整缓存架构，重点仍然是 RAG 主链路。

---

### 4. Sentinel 为什么用于 RAG 接口？

RAG 接口会调用外部大模型和 Embedding 服务，成本和延迟都较高。

Sentinel 限流可以防止短时间高频请求造成：

- API 额度快速消耗
- 接口响应变慢
- 外部服务压力过高
- 系统稳定性下降

---

## 十三、后续优化方向

后续可以从以下方向继续优化：

1. 将文档上传处理逻辑从 Controller 下沉到 Service。
2. 文档处理流程加入事务控制。
3. 使用异步任务处理大文件向量化。
4. 引入 pgvector 或 Milvus 优化向量检索。
5. 增加 Embedding 缓存，减少重复向量化成本。
6. 增加文档处理进度展示。
7. 增加前端页面，形成完整产品闭环。
8. 增加管理员后台和用户使用统计。
9. 增加大模型调用日志和 token 消耗统计。
10. 增加更完善的异常处理和接口参数校验。

---

## 十四、面试说明

这个项目的重点不是声称自己做了企业级 RAG 平台，而是展示自己理解并实现了 RAG 应用的核心链路。

面试中可以重点介绍：

- JWT 鉴权流程
- ThreadLocal 用户上下文
- 文档上传与解析
- 文本分块策略
- Embedding 向量化
- 余弦相似度计算
- TopK 检索
- Prompt 拼接
- Sentinel 限流
- Docker Compose 部署

面试中不建议夸大：

- 不说企业级高性能向量数据库
- 不说彻底解决大模型幻觉
- 不说 Redis 已经完整缓存 Embedding
- 不说优化 90% 计算量
- 不说生产级 RAG 平台

更稳妥的说法是：

```text
这是一个轻量级 RAG 后端系统，当前版本重点打通文档入库、向量化、相似度检索和大模型回答的完整主链路。
在小规模个人知识库场景下，这个方案可以正常演示和使用。
如果数据规模扩大，后续可以升级为 Milvus、pgvector 或 Elasticsearch dense_vector。
```

---

## 十五、项目总结

本项目完整实现了一个轻量级 AI 知识库后端系统。

通过这个项目，可以体现：

- Spring Boot 后端开发能力
- 数据库设计能力
- 用户认证和权限隔离意识
- 文档上传与解析能力
- RAG 基本原理理解
- 大模型 API 接入能力
- 工程部署和配置安全意识
- 对项目边界和后续优化方向的清晰认知

该项目适合作为 Java 后端实习面试中的主项目进行展示。