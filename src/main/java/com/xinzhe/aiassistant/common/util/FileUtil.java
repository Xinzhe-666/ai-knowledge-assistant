package com.xinzhe.aiassistant.common.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件上传工具类（修正版）
 */
@Component
@Slf4j
public class FileUtil {

    /**
     * 文件存储根路径
     */
    @Value("${file.upload-path:./uploads}")
    private String uploadPath;

    /**
     * 上传文件
     * @param file 前端上传的文件
     * @return 文件的绝对路径
     */
    public String uploadFile(MultipartFile file) {
        try {
            // 1. 生成唯一文件名
            String originalFilename = file.getOriginalFilename();
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + suffix;

            // 2. 获取绝对路径，确保目录存在
            Path absoluteUploadPath = Paths.get(uploadPath).toAbsolutePath().normalize();
            if (!Files.exists(absoluteUploadPath)) {
                Files.createDirectories(absoluteUploadPath);
                log.info("创建文件存储目录：{}", absoluteUploadPath);
            }

            // 3. 构建完整的文件保存路径
            Path filePath = absoluteUploadPath.resolve(uniqueFileName);

            // 4. 保存文件
            Files.copy(file.getInputStream(), filePath);

            log.info("文件上传成功，文件名：{}，存储路径：{}", originalFilename, filePath);
            return filePath.toString();

        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 删除本地文件
     * @param filePath 文件路径
     */
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(filePath);
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("文件删除成功，路径：{}", filePath);
            }
        } catch (Exception e) {
            log.error("文件删除失败", e);
        }
    }

    /**
     * 获取文件类型
     * @param fileName 文件名
     * @return 文件类型
     */
    public String getFileType(String fileName) {
        String suffix = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (suffix) {
            case "txt" -> "txt";
            case "md" -> "md";
            case "pdf" -> "pdf";
            case "docx" -> "docx";
            default -> throw new RuntimeException("不支持的文件格式，仅支持txt、md、pdf、docx");
        };
    }
}