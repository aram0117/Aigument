package com.example.aigument.common.aop;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;


@Slf4j
@Aspect
@Order(AopOrder.LATENCY)
@Component
@RequiredArgsConstructor
public class LatencyAop {

    private final MeterRegistry meterRegistry;

    /**
     * 대상 메서드를 감싸 실행 시간을 측정하고, 지연 로그 기록과 Prometheus용 Timer 메트릭 등록을 함께 수행합니다.
     */
    @Around("@annotation(com.example.aigument.common.annotation.MeasureLatency) "
            + "|| @within(com.example.aigument.common.annotation.MeasureLatency)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        String className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        String methodName = joinPoint.getSignature().getName();

        long startNanos = System.nanoTime();

        try {
            return joinPoint.proceed();
        } finally {
            long elapsedNanos = System.nanoTime() - startNanos;
            long elapsedMs = elapsedNanos / 1_000_000;

            log.info("[LatencyAop] {}.{} executed in {} ms", className, methodName, elapsedMs);

            // Grafana에서 aigument_method_latency_seconds 메트릭으로 조회 가능
            Timer.builder("aigument.method.latency")
                    .tag("class", className)
                    .tag("method", methodName)
                    .register(meterRegistry)
                    .record(elapsedNanos, TimeUnit.NANOSECONDS);
        }
    }
}
