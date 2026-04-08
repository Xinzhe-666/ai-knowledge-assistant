package com.xinzhe.aiassistant.controller;

import com.xinzhe.aiassistant.common.exception.BusinessException;
import com.xinzhe.aiassistant.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试接口
 * 用于验证通用模块是否正常工作
 */
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 测试成功返回
     * 访问地址：http://localhost:8080/test/success
     */
    @GetMapping("/success")
    public Result<String> testSuccess() {
        return Result.success("Hello AI Knowledge Assistant!");
    }

    /**
     * 测试业务异常
     * 访问地址：http://localhost:8080/test/business
     */
    @GetMapping("/business")
    public Result<Void> testBusinessException() {
        throw new BusinessException("这是一个业务异常测试");
    }

    /**
     * 测试系统异常
     * 访问地址：http://localhost:8080/test/system
     */
    @GetMapping("/system")
    public Result<Void> testSystemException() {
        // 故意制造一个空指针异常
        String str = null;
        str.length();
        return Result.success();
    }
}