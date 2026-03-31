package com.dduru.gildongmu.common.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class ServicePerformanceLoggingAspect {

    @Value("${app.logging.performance.warn-threshold-ms:300}")
    private long warnThresholdMs;

    @Around("execution(* com.dduru.gildongmu..service..*(..))")
    public Object logServiceExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String method = joinPoint.getSignature().toShortString();

        Object result = joinPoint.proceed();
        long elapsedMs = System.currentTimeMillis() - startTime;
        if (elapsedMs >= warnThresholdMs) {
            log.warn("SLOW SERVICE - method: {}, elapsedMs: {}", method, elapsedMs);
        } else {
            log.info("SERVICE OK - method: {}, elapsedMs: {}", method, elapsedMs);
        }
        return result;
    }
}
