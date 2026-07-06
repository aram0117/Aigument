package com.example.aigument.ai.model.ollama;

import com.example.aigument.common.exception.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static com.example.aigument.common.exception.ErrorCode.AI_COMMUNICATION_ERROR;
import static com.example.aigument.common.exception.ErrorCode.AI_RESPONSE_TIMEOUT;

@Slf4j
@Component
public class OllamaAi {

    private final WebClient webClient;
    private final String model;

    public OllamaAi(
            @Value("${ollama.base.url}") String baseUrl,
            @Value("${ai.model.name}") String model,
            @Value("${ai.model.ttl}") long minutes
    ) {
        this.model = model;
        this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .clientConnector(new ReactorClientHttpConnector(
                        HttpClient.create()
                                .responseTimeout(Duration.ofMinutes(minutes))
                ))
                .build();
    }

    public Mono<String> askOllama3(String prompt) {

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "prompt", prompt,
                "stream", false,
                "format", "json",
                "keep_alive", "1h"
        );

        return webClient.post()
                .uri("/api/generate")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Map.class)
                .map(responseBody -> responseBody.get("response").toString())
                // 에러 발생 시 원본 원인을 파악하기 위한 로깅
                .doOnError(e -> log.error("[OllamaAi] AI 서버 요청 실패 - 원인: {}", e.getMessage()))
                // 예외 타입에 따른 세밀한 매핑
                .onErrorMap(TimeoutException.class, e -> new CustomException(AI_RESPONSE_TIMEOUT))
                // CustomException이 아닌 기타 모든 에러는 통신 에러로 간주
                .onErrorMap(e -> !(e instanceof CustomException), e -> new CustomException(AI_COMMUNICATION_ERROR));
    }
}
