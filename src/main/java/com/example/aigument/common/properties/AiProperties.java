package com.example.aigument.common.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class AiProperties {

    @Value("${ollama.base.url}")
    private String ollamaBaseUrl;

    @Value("${ai.model.name}")
    private String modelName;

    @Value("${ai.model.ttl}") // Ollama 응답 타임아웃 (분)
    private long modelTtlMinutes;

    @Value("${ai.model.keep-alive:1h}") // Ollama 모델 메모리 유지 시간
    private String modelKeepAlive;

    @Value("${ai.retry.max-attempts:3}") // Ollama 요청 실패 시 재시도 횟수 (최초 요청 제외)
    private long retryMaxAttempts;

    @Value("${ai.retry.backoff.initial-seconds:2}") // 재시도 간 최초 대기 시간 (지수 백오프)
    private long retryBackoffInitialSeconds;

    @Value("${ai.retry.backoff.max-seconds:20}") // 재시도 간 최대 대기 시간
    private long retryBackoffMaxSeconds;
}
