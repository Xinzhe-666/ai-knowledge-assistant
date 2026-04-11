package com.xinzhe.aiassistant.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 文档解析工具类
 * 支持txt、md、pdf、docx格式，提取纯文本内容
 */
@Component
@Slf4j
public class DocumentParserUtil {

    /**
     * 解析文档，提取纯文本内容
     * @param filePath 文件本地路径
     * @param fileType 文件类型
     * @return 提取的纯文本内容
     */
    public String parseDocument(String filePath, String fileType) {
        try {
            return switch (fileType) {
                case "txt", "md" -> parseTxtOrMd(filePath);
                case "pdf" -> parsePdf(filePath);
                case "docx" -> parseDocx(filePath);
                default -> throw new RuntimeException("不支持的文件格式");
            };
        } catch (Exception e) {
            log.error("文档解析失败，文件路径：{}", filePath, e);
            throw new RuntimeException("文档解析失败，请检查文件格式是否正确");
        }
    }

    /**
     * 解析txt和md文件
     */
    private String parseTxtOrMd(String filePath) throws Exception {
        return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
    }

    /**
     * 解析PDF文件
     */
    private String parsePdf(String filePath) throws Exception {
        try (PDDocument document = PDDocument.load(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 解析docx文件
     */
    private String parseDocx(String filePath) throws Exception {
        try (FileInputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis);
             XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
            return extractor.getText();
        }
    }
}