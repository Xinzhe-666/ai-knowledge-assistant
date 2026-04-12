# 用Amazon Corretto 17替代OpenJDK，更稳定，国内更容易拉取
FROM amazoncorretto:17-alpine-jdk

# 设置工作目录
WORKDIR /app

# 复制jar包
COPY target/ai-knowledge-assistant-0.0.1-SNAPSHOT.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]