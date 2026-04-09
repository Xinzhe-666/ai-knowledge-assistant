package com.xinzhe.aiassistant.common.exception;

import com.xinzhe.aiassistant.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 捕获项目中所有的异常，统一封装成Result格式返回
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获我们自定义的业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        // 打印错误日志，方便排查问题
        log.error("业务异常：{}", e.getMessage(), e);
        // 封装成统一的Result格式返回
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 捕获参数校验异常
     * 比如前端传的参数为空、格式不对
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        // 获取第一个错误的提示信息
        String msg = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.error("参数校验异常：{}", msg, e);
        return Result.fail("参数错误：" + msg);
    }

    /**
     * 捕获所有其他异常（兜底处理）
     * 比如空指针异常、数据库异常等系统异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        // 系统异常不要把具体错误信息返回给前端，避免泄露代码
        return Result.fail("系统内部错误，请稍后重试");
    }
}
