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
}
