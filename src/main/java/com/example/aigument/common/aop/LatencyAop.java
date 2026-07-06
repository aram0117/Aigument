package com.example.aigument.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;


@Slf4j
@Aspect
@Order(AopOrder.LATENCY)
@Component
public class LatencyAop {

    /**
     * 대상 메서드를 감싸 실행 시간을 측정하고 지연 로그를 기록합니다.
     */
    @Around("@annotation(com.example.aigument.common.annotation.MeasureLatency) "
            + "|| @within(com.example.aigument.common.annotation.MeasureLatency)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        long startNanos = System.nanoTime();

        try {
            return joinPoint.proceed();
        } finally {
            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;

            log.info("[LatencyAop] {}.{} executed in {} ms",
                    joinPoint.getSignature().getDeclaringType().getSimpleName(),
                    joinPoint.getSignature().getName(),
                    elapsedMs);
        }
    }
}
