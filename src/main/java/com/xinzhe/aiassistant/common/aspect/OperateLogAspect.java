package com.xinzhe.aiassistant.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class OperateLogAspect {
    @Around("execution(* com.xinzhe.aiassistant.controller.*.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        String method = joinPoint.getSignature().toShortString();
        try {
            return joinPoint.proceed();
        } finally {
            long cost = System.currentTimeMillis() - start;
            if (cost > 500) {
                log.warn("【慢接口】{} 耗时: {}ms", method, cost);
            } else {
                log.info("【接口调用】{} 耗时: {}ms", method, cost);
            }
        }
    }
}